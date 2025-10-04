/**
 * Represents a player in the game with their name and difficulty level preference.
 */
public class Player {
    private String name;
    private int difficultyLevel;
    private int topScore;
    
    private static final String DEFAULT_PLAYER_NAME = "Master Chief";
    private static final int DEFAULT_DIFFICULTY_LEVEL = 1; // Easy
    private static final int DEFAULT_TOP_SCORE = 0;
    
    /**
     * Constructor to create a player with default values.
     */
    public Player() {
        this.name = DEFAULT_PLAYER_NAME;
        this.difficultyLevel = DEFAULT_DIFFICULTY_LEVEL;
        this.topScore = DEFAULT_TOP_SCORE;
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
        this.topScore = DEFAULT_TOP_SCORE;
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
     * Gets the player's top score.
     * 
     * @return The player's best score
     */
    public int getTopScore() {
        return topScore;
    }
    
    /**
     * Updates the player's top score if the new score is better (higher).
     * 
     * @param newScore The new score to compare with the current top score
     * @return true if the top score was updated, false otherwise
     */
    public boolean updateTopScore(int newScore) {
        if (newScore > topScore) {
            topScore = newScore;
            return true;
        }
        return false;
    }
    
    /**
     * Resets the player's top score to the default value.
     */
    public void resetTopScore() {
        this.topScore = DEFAULT_TOP_SCORE;
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
     * @return String representation containing name, difficulty level, and top score
     */
    @Override
    public String toString() {
        return "Player{name='" + name + "', difficultyLevel=" + difficultyLevel + ", topScore=" + topScore + "}";
    }
}