import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight in-memory scoreboard used by the Game Hub to record match
 * outcomes. This is intentionally simple and not persisted between runs.
 */
public final class Scoreboard {
    private final Map<String, Integer> wins = new HashMap<>();
    private int draws = 0;

    /**
     * Record a win for the specified player name.
     *
     * @param playerName player who won
     */
    public void recordWin(String playerName) {
        wins.put(playerName, wins.getOrDefault(playerName, 0) + 1);
    }

    /** Record a draw. */
    public void recordDraw() { draws++; }

    /**
     * Render the scoreboard contents as a human-readable string.
     *
     * @return textual summary of wins and draws
     */
    @Override public String toString() {
        if (wins.isEmpty() && draws == 0) return "(no results yet)";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Draws: %d\n", draws));
        for (Map.Entry<String,Integer> e : wins.entrySet()) {
            sb.append(String.format("%s : %d wins\n", e.getKey(), e.getValue()));
        }
        return sb.toString();
    }
}
