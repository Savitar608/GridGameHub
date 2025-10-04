import java.util.*;

/**
 * Represents a player in the game with their name and difficulty level preference.
 */
public class Player {
    private String name;
    private int difficultyLevel;
    private Map<Integer, Integer> topScores; // Map difficulty level to top score
    
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
     * @param name The player's name
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
     * Sets the player's difficulty level. Any positive difficulty level is accepted.
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
    public int getTopScore() {
        return getTopScore(difficultyLevel);
    }
    
    /**
     * Gets the player's top score for a specific difficulty level.
     * 
     * @param difficulty The difficulty level
     * @return The player's best score for the specified difficulty
     */
    public int getTopScore(int difficulty) {
        return topScores.getOrDefault(difficulty, DEFAULT_TOP_SCORE);
    }
    
    /**
     * Gets all top scores for all played difficulty levels.
     * 
     * @return Map of difficulty levels to top scores
     */
    public Map<Integer, Integer> getAllTopScores() {
        return new HashMap<>(topScores); // Return a copy to prevent external modification
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
     * Updates the player's top score for the current difficulty level if the new score is better (higher).
     * 
     * @param newScore The new score to compare with the current top score
     * @return true if the top score was updated, false otherwise
     */
    public boolean updateTopScore(int newScore) {
        return updateTopScore(newScore, difficultyLevel);
    }
    
    /**
     * Updates the player's top score for a specific difficulty level if the new score is better (higher).
     * 
     * @param newScore The new score to compare with the current top score
     * @param difficulty The difficulty level
     * @return true if the top score was updated, false otherwise
     */
    public boolean updateTopScore(int newScore, int difficulty) {
        int currentScore = topScores.getOrDefault(difficulty, DEFAULT_TOP_SCORE);
        if (newScore > currentScore) {
            topScores.put(difficulty, newScore);
            return true;
        }
        return false;
    }
    
    /**
     * Resets the player's top score for the current difficulty level.
     */
    public void resetTopScore() {
        resetTopScore(difficultyLevel);
    }
    
    /**
     * Resets the player's top score for a specific difficulty level.
     * 
     * @param difficulty The difficulty level
     */
    public void resetTopScore(int difficulty) {
        topScores.remove(difficulty);
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
     * @return String representation containing name, difficulty level, and top scores
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player{name='").append(name).append("', difficultyLevel=").append(difficultyLevel);
        sb.append(", topScores={");
        
        List<Integer> levels = getPlayedDifficultyLevels();
        for (int i = 0; i < levels.size(); i++) {
            int level = levels.get(i);
            sb.append(level).append(":").append(topScores.get(level));
            if (i < levels.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("}}");
        return sb.toString();
    }
}