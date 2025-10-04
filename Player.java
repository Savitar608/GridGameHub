
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
 * 
 * @author Adithya Lnu
 * @version 2.0
 * @date October 4, 2025
 * @course CS611 - Object Oriented Design
 * @assignment Assignment 1
 */

import java.util.*;

/**
 * Represents a player in the game with their name and difficulty level
 * preference.
 */
public class Player {
    private String name;
    private int difficultyLevel;
    private Map<Integer, Map<String, Integer>> topScores; // Map difficulty level -> (grid size -> top score)

    private static final String DEFAULT_PLAYER_NAME = "Master Chief";
    private static final int DEFAULT_DIFFICULTY_LEVEL = 1;
    private static final int DEFAULT_TOP_SCORE = 0;

    /**
     * Constructor to create a player with default values.
     */
    public Player() {
        this.name = DEFAULT_PLAYER_NAME;
        this.difficultyLevel = DEFAULT_DIFFICULTY_LEVEL;
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
     */
    public void resetTopScore(int rows, int cols) {
        resetTopScore(difficultyLevel, rows, cols);
    }

    /**
     * Resets the player's top score for a specific difficulty level.
     * 
     * @param difficulty The difficulty level
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
        return DEFAULT_PLAYER_NAME;
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

    private String toGridKey(int rows, int cols) {
        return rows + "x" + cols;
    }
}