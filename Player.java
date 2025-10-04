
/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 * 
 * File: Player.java
 * Description: Player class for managing player information including name,
 *              difficulty level preferences, and top scores tracking.
 *              Supports extensible difficulty levels and maintains separate
 *              top scores for each difficulty level.
 * 
 * Features:
 * - Player name management with validation
 * - Extensible difficulty level support (any positive integer)
 * - Top score tracking per difficulty level using Map structure
 * - Utility methods for score management and player information display
 * - Default value handling for robustness
 */

import java.util.*;
import java.util.Objects;

/**
 * Represents a player in the game with their name and difficulty level
 * preference.
 */
public class Player {
    private String name;
    private int difficultyLevel;
    private Map<Integer, Map<String, Integer>> topScores; // Map difficulty level -> (grid size -> top score)

    private static final String[] DEFAULT_PLAYER_NAME = {"Master Chief", "Lara Croft", "Mario", "Zelda", "Link", "Samus Aran", "Pikachu", "Kirby", "Sonic", "Tails"};
    private static final int DEFAULT_DIFFICULTY_LEVEL = 1;
    private static final int DEFAULT_TOP_SCORE = 0;

    /**
     * Constructor to create a player with default values.
     */
    public Player() {
        this.name = getDefaultPlayerName();
        this.difficultyLevel = getDefaultDifficultyLevel();
        this.topScores = new HashMap<>();
    }

    /**
     * Constructor to create a player with specified name and difficulty.
     * 
     * @param name            The player's name
     * @param difficultyLevel The player's chosen difficulty level
     */
    public Player(String name, int difficultyLevel) {
        setName(name);
        setDifficultyLevel(difficultyLevel);
        this.topScores = new HashMap<>();
    }

    /**
     * Sets the player's name. If the name is null or empty, uses the default name.
     * 
     * @param name The player's name
     */
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        } else {
            this.name = DEFAULT_PLAYER_NAME;
        }
    }

    /**
     * Gets the player's name.
     * 
     * @return The player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Prompts the player for a name using the provided services, applying default
     * handling when necessary.
     *
     * @param inputService  input source for the player's response
     * @param outputService destination for prompt and feedback messages
     * @return {@code true} if gameplay should continue, {@code false} when the
     *         player opts to quit
     */
    public boolean promptForName(InputService inputService, OutputService outputService) {
        Objects.requireNonNull(inputService, "inputService must not be null");
        Objects.requireNonNull(outputService, "outputService must not be null");

        outputService.print("Enter player name (type 'quit' to exit): ");
        String playerName = inputService.readLine();

        if (playerName == null) {
            return false;
        }

        String trimmedName = playerName.trim();
        if ("quit".equalsIgnoreCase(trimmedName)) {
            return false;
        }

        if (!trimmedName.isEmpty()) {
            setName(trimmedName);
        } else {
            outputService.println("Warning: Invalid name provided. Setting default name.");
            setName(getDefaultPlayerName());
        }

        return true;
    }

    /**
     * Sets the player's difficulty level. Any positive difficulty level is
     * accepted.
     * 
     * @param difficultyLevel The difficulty level (any positive integer)
     */
    public void setDifficultyLevel(int difficultyLevel) {
        if (difficultyLevel >= 1) {
            this.difficultyLevel = difficultyLevel;
        } else {
            this.difficultyLevel = DEFAULT_DIFFICULTY_LEVEL;
        }
    }

    /**
     * Gets the player's difficulty level.
     * 
     * @return The difficulty level
     */
    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    /**
     * Gets the player's top score for the current difficulty level.
     * 
     * @param rows The number of rows in the grid
     * @param cols The number of columns in the grid
     * @return The player's best score for current difficulty
     */
    public int getTopScore(int rows, int cols) {
        return getTopScore(difficultyLevel, rows, cols);
    }

    /**
     * Gets the player's top score for a specific difficulty level.
     * 
     * @param difficulty The difficulty level
     * @param rows       The number of rows in the grid
     * @param cols       The number of columns in the grid
     * @return The player's best score for the specified difficulty
     */
    public int getTopScore(int difficulty, int rows, int cols) {
        Map<String, Integer> difficultyScores = topScores.get(difficulty);
        if (difficultyScores == null) {
            return DEFAULT_TOP_SCORE;
        }
        return difficultyScores.getOrDefault(toGridKey(rows, cols), DEFAULT_TOP_SCORE);
    }

    /**
     * Gets all top scores for all played difficulty levels.
     * 
     * @return Map of difficulty levels to top scores
     */
    public Map<Integer, Map<String, Integer>> getAllTopScores() {
        Map<Integer, Map<String, Integer>> topScoresMap = new HashMap<>();
        for (Map.Entry<Integer, Map<String, Integer>> entry : topScores.entrySet()) {
            topScoresMap.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        return topScoresMap;
    }

    /**
     * Gets a sorted list of all difficulty levels that have been played.
     * 
     * @return Sorted list of difficulty levels
     */
    public List<Integer> getPlayedDifficultyLevels() {
        List<Integer> levels = new ArrayList<>(topScores.keySet());
        Collections.sort(levels);
        return levels;
    }

    /**
     * Updates the player's top score for the current difficulty level if the new
     * score is better (higher).
     * 
     * @param newScore The new score to compare with the current top score
     * @param rows     The number of rows in the grid
     * @param cols     The number of columns in the grid
     * @return true if the top score was updated, false otherwise
     */
    public boolean updateTopScore(int newScore, int rows, int cols) {
        return updateTopScore(newScore, difficultyLevel, rows, cols);
    }

    /**
     * Updates the player's top score for a specific difficulty level if the new
     * score is better (higher).
     * 
     * @param newScore   The new score to compare with the current top score
     * @param difficulty The difficulty level
     * @param rows       The number of rows in the grid
     * @param cols       The number of columns in the grid
     * @return true if the top score was updated, false otherwise
     */
    public boolean updateTopScore(int newScore, int difficulty, int rows, int cols) {
        String gridKey = toGridKey(rows, cols);
        Map<String, Integer> difficultyScores = topScores.computeIfAbsent(difficulty, key -> new HashMap<>());
        int currentScore = difficultyScores.getOrDefault(gridKey, DEFAULT_TOP_SCORE);
        if (newScore > currentScore) {
            difficultyScores.put(gridKey, newScore);
            return true;
        }
        return false;
    }

    /**
     * Resets the player's top score for the current difficulty level.
     *
     * @param rows The number of rows in the grid
     * @param cols The number of columns in the grid
     */
    public void resetTopScore(int rows, int cols) {
        resetTopScore(difficultyLevel, rows, cols);
    }

    /**
     * Resets the player's top score for a specific difficulty level.
     * 
     * @param difficulty The difficulty level
     * @param rows       The number of rows in the grid
     * @param cols       The number of columns in the grid
     */
    public void resetTopScore(int difficulty, int rows, int cols) {
        Map<String, Integer> difficultyScores = topScores.get(difficulty);
        if (difficultyScores != null) {
            difficultyScores.remove(toGridKey(rows, cols));
            if (difficultyScores.isEmpty()) {
                topScores.remove(difficulty);
            }
        }
    }

    /**
     * Resets all top scores for all difficulty levels.
     */
    public void resetAllTopScores() {
        topScores.clear();
    }

    /**
     * Gets the default player name.
     * 
     * @return The default player name
     */
    public static String getDefaultPlayerName() {
        return DEFAULT_PLAYER_NAME[new Random().nextInt(DEFAULT_PLAYER_NAME.length)];
    }

    /**
     * Gets the default difficulty level.
     * 
     * @return The default difficulty level
     */
    public static int getDefaultDifficultyLevel() {
        return DEFAULT_DIFFICULTY_LEVEL;
    }

    /**
     * Returns a string representation of the player.
     * 
     * @return String representation containing name, difficulty level, and top
     *         scores
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player{name='").append(name).append("', difficultyLevel=").append(difficultyLevel);
        sb.append(", topScores={");

        List<Integer> levels = getPlayedDifficultyLevels();
        for (int i = 0; i < levels.size(); i++) {
            int level = levels.get(i);
            sb.append(level).append("=").append(topScores.get(level));
            if (i < levels.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("}}");
        return sb.toString();
    }

    /**
     * Generates a stable key representing the supplied grid dimensions for use in
     * score maps.
     *
     * @param rows number of rows in the puzzle grid
     * @param cols number of columns in the puzzle grid
     * @return concatenated representation in the form {@code rows x cols}
     */
    private String toGridKey(int rows, int cols) {
        return rows + "x" + cols;
    }
}