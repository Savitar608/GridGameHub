/**
 * Represents a player in the game with their name and difficulty level preference.
 */
public class Player {
    private String name;
    private int difficultyLevel;
    private int[] topScores; // Index 0=Easy, 1=Medium, 2=Hard
    
    private static final String DEFAULT_PLAYER_NAME = "Master Chief";
    private static final int DEFAULT_DIFFICULTY_LEVEL = 1; // Easy
    private static final int DEFAULT_TOP_SCORE = 0;
    
    /**
     * Constructor to create a player with default values.
     */
    public Player() {
        this.name = DEFAULT_PLAYER_NAME;
        this.difficultyLevel = DEFAULT_DIFFICULTY_LEVEL;
        this.topScores = new int[3]; // Easy, Medium, Hard
        // Initialize all scores to 0
        for (int i = 0; i < topScores.length; i++) {
            topScores[i] = DEFAULT_TOP_SCORE;
        }
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
        this.topScores = new int[3]; // Easy, Medium, Hard
        // Initialize all scores to 0
        for (int i = 0; i < topScores.length; i++) {
            topScores[i] = DEFAULT_TOP_SCORE;
        }
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
     * Sets the player's difficulty level. If the level is invalid, uses the default level.
     * 
     * @param difficultyLevel The difficulty level (1-3)
     */
    public void setDifficultyLevel(int difficultyLevel) {
        if (difficultyLevel >= 1 && difficultyLevel <= 3) {
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
     * @param difficulty The difficulty level (1-3)
     * @return The player's best score for the specified difficulty
     */
    public int getTopScore(int difficulty) {
        if (difficulty >= 1 && difficulty <= 3) {
            return topScores[difficulty - 1]; // Convert to 0-based index
        }
        return DEFAULT_TOP_SCORE;
    }
    
    /**
     * Gets all top scores for all difficulty levels.
     * 
     * @return Array of top scores [Easy, Medium, Hard]
     */
    public int[] getAllTopScores() {
        return topScores.clone(); // Return a copy to prevent external modification
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
     * @param difficulty The difficulty level (1-3)
     * @return true if the top score was updated, false otherwise
     */
    public boolean updateTopScore(int newScore, int difficulty) {
        if (difficulty >= 1 && difficulty <= 3) {
            int index = difficulty - 1; // Convert to 0-based index
            if (newScore > topScores[index]) {
                topScores[index] = newScore;
                return true;
            }
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
     * @param difficulty The difficulty level (1-3)
     */
    public void resetTopScore(int difficulty) {
        if (difficulty >= 1 && difficulty <= 3) {
            topScores[difficulty - 1] = DEFAULT_TOP_SCORE;
        }
    }
    
    /**
     * Resets all top scores for all difficulty levels.
     */
    public void resetAllTopScores() {
        for (int i = 0; i < topScores.length; i++) {
            topScores[i] = DEFAULT_TOP_SCORE;
        }
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
        return "Player{name='" + name + "', difficultyLevel=" + difficultyLevel + 
               ", topScores=[Easy:" + topScores[0] + ", Medium:" + topScores[1] + ", Hard:" + topScores[2] + "]}";
    }
}