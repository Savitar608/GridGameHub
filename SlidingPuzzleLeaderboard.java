/**
 * File: SlidingPuzzleLeaderboard.java
 * Description: Manages persistent leaderboard storage for the Sliding Puzzle game,
 *              handling JSON serialization, ranking, and snapshot retrieval.
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Persistent leaderboard management for the Sliding Puzzle game. Stores player
 * scores in a local JSON file and exposes helper methods for displaying the top
 * performers.
 */
public final class SlidingPuzzleLeaderboard {
    private static final Path LEADERBOARD_FILE = Paths.get("sliding_puzzle_leaderboard.json");
    private static final int MAX_HISTORY_ENTRIES = 500;
    private static final int LEADERBOARD_LIMIT = 10;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    /**
     * Records a completed game score to persistent storage, trimming historical
     * data and returning an updated leaderboard snapshot focused on the player.
     *
     * @param playerName      name of the player finishing the game
     * @param score           score achieved
     * @param rows            board row count
     * @param cols            board column count
     * @param difficultyLabel difficulty name shown in the leaderboard
     * @param difficultyLevel numeric difficulty level
     * @return snapshot containing ordered leaderboard entries
     */
    public static LeaderboardSnapshot recordScore(String playerName, int score, int rows, int cols,
        String difficultyLabel, int difficultyLevel) {
        List<LeaderboardEntry> history = loadEntries();
        history.add(new LeaderboardEntry(playerName, score, rows, cols, difficultyLabel, difficultyLevel,
                Instant.now().toEpochMilli()));
        pruneHistory(history);
        saveEntries(history);
        return buildSnapshot(history, normalizePlayerKey(playerName));
    }

    /**
     * Retrieves a snapshot of the leaderboard without recording a new score.
     *
     * @return leaderboard snapshot reflecting the current persisted data
     */
    public static LeaderboardSnapshot getSnapshot() {
        List<LeaderboardEntry> history = loadEntries();
        return buildSnapshot(history, null);
    }

    /**
     * Retrieves the highest scoring entries recorded for the specified grid size.
     *
     * @param rows number of puzzle rows to filter by
     * @param cols number of puzzle columns to filter by
     * @return ordered list of top entries (may be empty)
     */
    public static List<LeaderboardEntry> getTopEntriesForGrid(int rows, int cols) {
        List<LeaderboardEntry> history = loadEntries();
        if (history.isEmpty()) {
            return Collections.emptyList();
        }

        List<LeaderboardEntry> matches = new ArrayList<>();
        for (LeaderboardEntry entry : history) {
            if (entry.getRows() == rows && entry.getCols() == cols) {
                matches.add(entry);
            }
        }

        if (matches.isEmpty()) {
            return Collections.emptyList();
        }

        matches.sort(Comparator.comparingInt(LeaderboardEntry::getScore).reversed()
                .thenComparingLong(LeaderboardEntry::getRecordedAt)
                .thenComparing(entry -> entry.getPlayerName().toLowerCase(Locale.ROOT)));

        if (matches.size() > LEADERBOARD_LIMIT) {
            matches = new ArrayList<>(matches.subList(0, LEADERBOARD_LIMIT));
        }

        return Collections.unmodifiableList(matches);
    }

    /**
     * Loads all leaderboard entries from disk, returning an empty list when the
     * file is absent or unreadable.
     */
    private static List<LeaderboardEntry> loadEntries() {
        if (!Files.exists(LEADERBOARD_FILE)) {
            return new ArrayList<>();
        }
        try {
            byte[] bytes = Files.readAllBytes(LEADERBOARD_FILE);
            String json = new String(bytes, StandardCharsets.UTF_8);
            return parseEntries(json);
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    /**
     * Orders entries by recency and trims excess history beyond the configured
     * retention limit.
     *
     * @param history mutable list of entries to prune
     */
    private static void pruneHistory(List<LeaderboardEntry> history) {
        history.sort(Comparator.comparingLong(LeaderboardEntry::getRecordedAt).reversed());
        if (history.size() > MAX_HISTORY_ENTRIES) {
            history.subList(MAX_HISTORY_ENTRIES, history.size()).clear();
        }
    }

    /**
     * Persists the provided history to the leaderboard JSON file.
     *
     * @param history leaderboard entries sorted from newest to oldest
     */
    private static void saveEntries(List<LeaderboardEntry> history) {
        List<LeaderboardEntry> copy = new ArrayList<>(history);
        copy.sort(Comparator.comparingLong(LeaderboardEntry::getRecordedAt).reversed());
        StringBuilder builder = new StringBuilder();
        builder.append("{\n  \"scores\": [\n");
        for (int i = 0; i < copy.size(); i++) {
            builder.append("    ").append(copy.get(i).toJson());
            if (i < copy.size() - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }
        builder.append("  ]\n}");
        try {
            Files.write(LEADERBOARD_FILE, builder.toString().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            // Silently ignore persistence errors
        }
    }

    /**
     * Parses stored JSON into leaderboard entries, ignoring malformed segments.
     *
     * @param json raw JSON content
     * @return parsed leaderboard entries (possibly empty)
     */
    private static List<LeaderboardEntry> parseEntries(String json) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) {
            return entries;
        }
        int start = json.indexOf('[');
        int end = json.lastIndexOf(']');
        if (start < 0 || end < start) {
            return entries;
        }
        String content = json.substring(start + 1, end);
        int depth = 0;
        int segmentStart = -1;
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (ch == '{') {
                if (depth == 0) {
                    segmentStart = i;
                }
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0 && segmentStart >= 0) {
                    String object = content.substring(segmentStart, i + 1);
                    LeaderboardEntry entry = parseEntryObject(object);
                    if (entry != null) {
                        entries.add(entry);
                    }
                    segmentStart = -1;
                }
            }
        }
        return entries;
    }

    /**
     * Converts a single JSON object representing a score into a
     * {@link LeaderboardEntry}.
     *
     * @param jsonObject JSON object text
     * @return entry instance or {@code null} if required fields are missing
     */
    private static LeaderboardEntry parseEntryObject(String jsonObject) {
        String player = extractString(jsonObject, "player");
        Integer score = extractInt(jsonObject, "score");
        Integer rows = extractInt(jsonObject, "rows");
        Integer cols = extractInt(jsonObject, "cols");
        String difficulty = extractString(jsonObject, "difficulty");
        Integer difficultyLevel = extractInt(jsonObject, "difficultyLevel");
        Long recordedAt = extractLong(jsonObject, "recordedAt");
        if (player == null || score == null || rows == null || cols == null || recordedAt == null) {
            return null;
        }
        if (difficulty == null) {
            difficulty = "";
        }
        if (difficultyLevel == null) {
            difficultyLevel = 0;
        }
        return new LeaderboardEntry(player, score, rows, cols, difficulty, difficultyLevel, recordedAt);
    }

    /**
     * Extracts a string value for the given key from a JSON object, handling escaped characters.
     */
    private static String extractString(String jsonObject, String key) {
        Pattern pattern = Pattern
                .compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"");
        Matcher matcher = pattern.matcher(jsonObject);
        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }
        return null;
    }

    /**
     * Extracts an integer value for the given key from a JSON object.
     */
    private static Integer extractInt(String jsonObject, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(jsonObject);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * Extracts a long value for the given key from a JSON object.
     */
    private static Long extractLong(String jsonObject, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(jsonObject);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * Builds an immutable snapshot of the leaderboard, including a player's
     * personal best when requested.
     *
     * @param history         all known leaderboard entries
     * @param focusPlayerKey  normalized player key to highlight, or {@code null}
     * @return computed snapshot
     */
    private static LeaderboardSnapshot buildSnapshot(List<LeaderboardEntry> history, String focusPlayerKey) {
        if (history.isEmpty()) {
            return new LeaderboardSnapshot(Collections.emptyList(), null, -1);
        }

        Map<String, LeaderboardEntry> bestByPlayer = new LinkedHashMap<>();
        for (LeaderboardEntry entry : history) {
            String key = normalizePlayerKey(entry.getPlayerName());
            LeaderboardEntry current = bestByPlayer.get(key);
            if (current == null || entry.getScore() > current.getScore()
                    || (entry.getScore() == current.getScore() && entry.getRecordedAt() < current.getRecordedAt())) {
                bestByPlayer.put(key, entry);
            }
        }

        List<LeaderboardEntry> orderedBest = new ArrayList<>(bestByPlayer.values());
        orderedBest.sort(Comparator.comparingInt(LeaderboardEntry::getScore).reversed()
                .thenComparingLong(LeaderboardEntry::getRecordedAt)
                .thenComparing(entry -> entry.getPlayerName().toLowerCase(Locale.ROOT)));

        List<LeaderboardEntry> displayEntries = orderedBest.size() > LEADERBOARD_LIMIT
                ? new ArrayList<>(orderedBest.subList(0, LEADERBOARD_LIMIT))
                : new ArrayList<>(orderedBest);

        LeaderboardEntry playerBest = null;
        int rank = -1;
        if (focusPlayerKey != null && !focusPlayerKey.isEmpty()) {
            for (int i = 0; i < orderedBest.size(); i++) {
                LeaderboardEntry entry = orderedBest.get(i);
                if (normalizePlayerKey(entry.getPlayerName()).equals(focusPlayerKey)) {
                    rank = i + 1;
                    playerBest = entry;
                    break;
                }
            }
        }

        return new LeaderboardSnapshot(Collections.unmodifiableList(displayEntries), playerBest, rank);
    }

    /**
     * Generates a normalized, case-insensitive key for player comparisons.
     */
    private static String normalizePlayerKey(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Escapes special characters for safe embedding inside JSON strings.
     */
    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\\", "\\\\").replace("\"", "\\\"");
        escaped = escaped.replace("\n", "\\n").replace("\r", "\\r");
        return escaped;
    }

    /**
     * Replaces escaped JSON sequences with their literal counterparts.
     */
    private static String unescapeJson(String value) {
        if (value == null) {
            return null;
        }
        String unescaped = value.replace("\\n", "\n").replace("\\r", "\r");
        unescaped = unescaped.replace("\\\"", "\"").replace("\\\\", "\\");
        return unescaped;
    }

    /**
     * Immutable view of leaderboard standings and an optional focused player's
     * ranking.
     */
    public static final class LeaderboardSnapshot {
        private final List<LeaderboardEntry> topEntries;
        private final LeaderboardEntry playerBest;
        private final int playerRank;

        private LeaderboardSnapshot(List<LeaderboardEntry> topEntries, LeaderboardEntry playerBest, int playerRank) {
            this.topEntries = topEntries;
            this.playerBest = playerBest;
            this.playerRank = playerRank;
        }

        /**
         * @return ordered list of top leaderboard entries
         */
        public List<LeaderboardEntry> getTopEntries() {
            return topEntries;
        }

        /**
         * @return highlighted player's best entry, or {@code null} when absent
         */
        public LeaderboardEntry getPlayerBest() {
            return playerBest;
        }

        /**
         * @return one-based rank of the highlighted player, or {@code -1} when unknown
         */
        public int getPlayerRank() {
            return playerRank;
        }
    }

    /**
     * Value object representing a single recorded leaderboard score.
     */
    public static final class LeaderboardEntry {
        private final String playerName;
        private final int score;
        private final int rows;
        private final int cols;
        private final String difficultyLabel;
        private final int difficultyLevel;
        private final long recordedAt;

        private LeaderboardEntry(String playerName, int score, int rows, int cols, String difficultyLabel,
                int difficultyLevel, long recordedAt) {
            this.playerName = playerName == null ? "Unknown" : playerName;
            this.score = score;
            this.rows = rows;
            this.cols = cols;
            this.difficultyLabel = difficultyLabel == null ? "" : difficultyLabel;
            this.difficultyLevel = difficultyLevel;
            this.recordedAt = recordedAt;
        }

        /**
         * @return recorded player name
         */
        public String getPlayerName() {
            return playerName;
        }

        /**
         * @return recorded score value
         */
        public int getScore() {
            return score;
        }

        /**
         * @return board row count for the recorded game
         */
        public int getRows() {
            return rows;
        }

        /**
         * @return board column count for the recorded game
         */
        public int getCols() {
            return cols;
        }

        /**
         * @return human-readable difficulty label
         */
        public String getDifficultyLabel() {
            return difficultyLabel;
        }

        /**
         * @return numeric difficulty level
         */
        public int getDifficultyLevel() {
            return difficultyLevel;
        }

        /**
         * @return timestamp (epoch millis) when the score was recorded
         */
        public long getRecordedAt() {
            return recordedAt;
        }

        /**
         * @return formatted grid size label (e.g., "3x3")
         */
        public String getGridLabel() {
            return rows + "x" + cols;
        }

        /**
         * @return difficulty label with fallback to numeric level
         */
        public String getDifficultyDisplay() {
            if (difficultyLabel != null && !difficultyLabel.trim().isEmpty()) {
                return difficultyLabel;
            }
            if (difficultyLevel > 0) {
                return "Level " + difficultyLevel;
            }
            return "Unknown";
        }

        /**
         * @return ISO-8601 formatted timestamp string
         */
        public String getRecordedAtIso() {
            return ISO_FORMATTER.format(Instant.ofEpochMilli(recordedAt));
        }

    /**
     * Serializes the entry into a compact JSON object representation.
     */
    private String toJson() {
            StringBuilder builder = new StringBuilder();
            builder.append("{")
                    .append("\"player\":\"").append(escapeJson(playerName)).append("\",")
                    .append("\"score\":").append(score).append(",")
                    .append("\"rows\":").append(rows).append(",")
                    .append("\"cols\":").append(cols).append(",")
                    .append("\"difficulty\":\"").append(escapeJson(difficultyLabel)).append("\",")
                    .append("\"difficultyLevel\":").append(difficultyLevel).append(",")
                    .append("\"recordedAt\":").append(recordedAt)
                    .append("}");
            return builder.toString();
        }
    }
}
