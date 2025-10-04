import java.util.*;
import java.lang.reflect.Array;

/**
 * An abstract base class for grid-based terminal games.
 * It provides the foundational structure for a game board, game state,
 * and the main game loop.
 * It can be extended to create games such as a sliding puzzle game
 *
 * @param <T> The type of elements stored in the grid.
 */
abstract class GridGame<T> {
    protected int rows;
    protected int cols;
    protected T[][] grid;
    protected boolean isGameOver;
    protected Scanner scanner;
    protected Player player;
    protected Map<Integer, String> difficultyLevels;
    protected int minDifficultyLevel;
    protected int maxDifficultyLevel;

    /**
     * Constructor to initialize the game.
     *
     * @param rows The number of rows in the grid.
     * @param cols The number of columns in the grid.
     */
    public GridGame(Class<T> componentType, int rows, int cols) {
        if (rows < 1 || cols < 1) {
            throw new IllegalArgumentException("Row and column sizes must be positive integers.");
        }

        @SuppressWarnings("unchecked")
        // Create a 2D generic array properly using reflection
        // Spent a lot of time figuring this out
        T[][] tempGrid = (T[][]) Array.newInstance(componentType, rows, 0);
        for (int i = 0; i < rows; i++) {
            tempGrid[i] = (T[]) Array.newInstance(componentType, cols);
        }

        // Initialize the grid and game state
        this.rows = rows;
        this.cols = cols;
        this.grid = tempGrid;
        this.isGameOver = false;
        this.scanner = new Scanner(System.in);
        this.player = new Player(); // Initialize with default values
        
        // Initialize default difficulty levels (can be overridden by subclasses)
        initializeDefaultDifficultyLevels();
    }
    
    /**
     * Initializes the default difficulty levels. Can be overridden by subclasses.
     */
    protected void initializeDefaultDifficultyLevels() {
        difficultyLevels = new LinkedHashMap<>(); // Preserve insertion order
        difficultyLevels.put(1, "Easy");
        difficultyLevels.put(2, "Medium");
        difficultyLevels.put(3, "Hard");
        
        minDifficultyLevel = Collections.min(difficultyLevels.keySet());
        maxDifficultyLevel = Collections.max(difficultyLevels.keySet());
    }
    
    /**
     * Adds a new difficulty level. Useful for extending the game with custom difficulties.
     * 
     * @param level The difficulty level number
     * @param name The name/description of the difficulty level
     */
    protected void addDifficultyLevel(int level, String name) {
        difficultyLevels.put(level, name);
        minDifficultyLevel = Math.min(minDifficultyLevel, level);
        maxDifficultyLevel = Math.max(maxDifficultyLevel, level);
    }
    
    /**
     * Gets the name of a difficulty level.
     * 
     * @param level The difficulty level number
     * @return The name of the difficulty level, or "Unknown" if not found
     */
    protected String getDifficultyName(int level) {
        return difficultyLevels.getOrDefault(level, "Unknown");
    }
    
    /**
     * Checks if a difficulty level is valid.
     * 
     * @param level The difficulty level to check
     * @return true if the level is valid, false otherwise
     */
    protected boolean isValidDifficultyLevel(int level) {
        return difficultyLevels.containsKey(level);
    }

    /**
     * Starts the game by displaying the welcome message and setting the player
     * name.
     */
    public void startGame() {
        this.isGameOver = false; // Reset game over status
        displayWelcomeMessage();
        setPlayerName();

        // Start the main game loop
        play();
    }

    /**
     * The main public method to start and run the game.
     */
    protected void play() {
        setSize();
        setDifficultyLevel();
        initializeGame();

        // Looping over the game until it's over
        while (!isGameOver) {
            displayGrid();
            processUserInput();

            // Check for win condition after each move
            // If the game is won, set isGameOver to true and break the loop
            isGameOver = checkWinCondition();
        }

        displayGrid();
        displayWinMessage();
    }

    /* Abstract Methods to be Implemented by Child Classes */

    /**
     * Validates the size of the grid.
     */
    protected abstract void validateSize();

    protected void setPlayerName() {
        System.out.print("Enter player name: ");
        String playerName = scanner.nextLine();

        if (playerName != null && !playerName.trim().isEmpty()) {
            this.player.setName(playerName.trim());
        } else {
            // setting a default name and displaying a warning
            System.out.println("Warning: Invalid name provided. Setting default name.");
            this.player.setName(Player.getDefaultPlayerName()); // Default name
        }
    }

    /**
     * Returns the player's name for display purposes.
     *
     * @return the player's name.
     */
    protected String getPlayerInfo() {
        // Return the player's name
        return player.getName();
    }

    /**
     * Sets the size of the grid.
     */
    protected abstract void setSize();

    /**
     * Sets the difficulty level of the game.
     *
     * @param level The difficulty level (1 to 3).
     */
    protected void setDifficultyLevel() {
        // Build difficulty options string
        StringBuilder optionsBuilder = new StringBuilder();
        List<Integer> levels = new ArrayList<>(difficultyLevels.keySet());
        Collections.sort(levels);
        
        for (int i = 0; i < levels.size(); i++) {
            int level = levels.get(i);
            optionsBuilder.append(level).append(" (").append(difficultyLevels.get(level)).append(")");
            if (i < levels.size() - 1) {
                optionsBuilder.append(", ");
            }
        }
        
        System.out.println("Hey " + getPlayerInfo() + ", choose your difficulty level: " + optionsBuilder.toString());
        System.out.println("Note: The difficulty level increases exponentially with grid size");
        String chosenLevel = scanner.nextLine();

        int level;
        try {
            // Parse the input to an integer
            level = Integer.parseInt(chosenLevel);
        } catch (Exception e) {
            // If parsing fails, default to minimum difficulty
            System.out.println("Invalid input. Defaulting to " + getDifficultyName(minDifficultyLevel) + ".");
            level = minDifficultyLevel;
        }

        // Set the level (Player class will validate and default if invalid)
        player.setDifficultyLevel(level);
        
        // Check if the level was changed due to validation
        if (!isValidDifficultyLevel(level)) {
            System.out.println("Invalid level. Defaulting to " + getDifficultyName(minDifficultyLevel) + ".");
            player.setDifficultyLevel(minDifficultyLevel);
        }

        System.out.println("Difficulty set to: " + getDifficultyName(player.getDifficultyLevel()));
        
        // Show current top score for this difficulty
        int currentTopScore = player.getTopScore();
        if (currentTopScore > 0) {
            System.out.println("Your current top score for this difficulty: " + currentTopScore);
        } else {
            System.out.println("No previous score for this difficulty level.");
        }

        // Show difficulty-specific warnings or tips
        showDifficultySpecificMessage(player.getDifficultyLevel());
    }
    
    /**
     * Shows difficulty-specific messages. Can be overridden by subclasses.
     * 
     * @param difficultyLevel The current difficulty level
     */
    protected void showDifficultySpecificMessage(int difficultyLevel) {
        // Show different messages based on difficulty level
        if (difficultyLevel >= 4) {
            System.out.println("🔥 EXTREME DIFFICULTY ACTIVATED! 🔥");
            System.out.println("Warning: " + getDifficultyName(difficultyLevel) + " mode is for seasoned puzzle masters!");
            System.out.println("Tip: Take your time and think several moves ahead. Consider using pen and paper to track your strategy.");
        } else if (difficultyLevel == 3) {
            System.out.println("Warning: " + getDifficultyName(difficultyLevel) + " mode can be quite challenging!");
            System.out.println("Tip: Plan your moves ahead and try to visualize the solution.");
        } else if (difficultyLevel == 1) {
            System.out.println("Perfect for beginners! Take your time to learn the game mechanics.");
        }
    }

    /**
     * Prints the game's welcome message and rules.
     */
    protected abstract void displayWelcomeMessage();

    /**
     * Sets up the initial state of the grid and game variables.
     */
    protected abstract void initializeGame();

    /**
     * Renders the current state of the grid to the console.
     */
    protected abstract void displayGrid();

    /**
     * Waits for and handles input from the player.
     */
    protected abstract void processUserInput();

    /**
     * Executes the player's move on the grid.
     *
     * @param row The row index of the move.
     * @param col The column index of the move.
     */
    protected abstract void makeMove(int row, int col);

    /**
     * Prints a message indicating that the player's input was invalid.
     */
    protected abstract void displayInvalidInputMessage();

    /**
     * Checks if the game's winning condition has been met.
     *
     * @return true if the game is won, false otherwise.
     */
    protected abstract boolean checkWinCondition();

    /**
     * Prints the final congratulatory message to the player.
     */
    protected abstract void displayWinMessage();
}

class SlidingPuzzleGame extends GridGame<Integer> {
    // Constraints on grid size
    public static final int MIN_SIZE = 3; // Minimum size allowed
    public static final int MAX_SIZE = 20; // Maximum size allowed

    // Position of the empty cell
    private int emptyRow;
    private int emptyCol;
    
    // Score tracking
    private int currentScore;
    private int moveCount;
    private long startTime;

    // Default values
    private static final int DEFAULT_ROWS = 3; // Default rows
    private static final int DEFAULT_COLS = 3; // Default columns

    // Border characters and empty cell representation
    private String emptyCell = "  ";
    private String topLeftCorner = "+";
    private String horizontalBorder = "--+";
    private String verticalBorder = "|";
    private String cellFormat = "%2d";

    /**
     * Validates the size of the grid.
     */
    protected void validateSize() {
        if (rows < MIN_SIZE || cols < MIN_SIZE) {
            throw new IllegalArgumentException("Grid size must be at least " + MIN_SIZE + "x" + MIN_SIZE);
        }
    }

    /**
     * Sets the size of the grid based on user input.
     */
    protected void setSize() {
        System.out.print("Enter grid size (rows x cols) (Min " + MIN_SIZE + ", Max " + MAX_SIZE + "): ");
        String sizeInput = scanner.nextLine();
        String[] parts = sizeInput.trim().split("\\s*x\\s*");

        if (parts.length == 2) {
            try {
                int rows = Integer.parseInt(parts[0]);
                int cols = Integer.parseInt(parts[1]);

                // if the size is the default size, skip reinitialization
                if (rows == this.rows && cols == this.cols) {
                    return;
                }

                // Validate the size against max constraints
                if (rows < MIN_SIZE || cols < MIN_SIZE || rows > MAX_SIZE || cols > MAX_SIZE) {
                    System.out.println("Invalid size. Using default size of " + DEFAULT_ROWS + "x" + DEFAULT_COLS + ".");
                    rows = DEFAULT_ROWS;
                    cols = DEFAULT_COLS;
                }

                // Reinitialize the grid with the new size using Integer array directly
                // Set the new size
                this.rows = rows;
                this.cols = cols;

                // Reinitialize the grid with the new size using Integer array directly
                this.grid = new Integer[rows][cols];

                // Adjust border and cell format based on grid size
                if (rows * cols < 100) {
                    horizontalBorder = "--+";
                    emptyCell = "  ";
                    cellFormat = "%2d";
                } else {
                    horizontalBorder = "---+";
                    emptyCell = "   ";
                    cellFormat = "%3d";
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Using default size of " + DEFAULT_ROWS + "x" + DEFAULT_COLS + ".");
            }
        } else {
            System.out.println("Invalid input. Using default size of " + DEFAULT_ROWS + "x" + DEFAULT_COLS + ".");
        }
        // Validate the size
        validateSize();
    }

    /**
     * Determines the number of shuffle moves based on the difficulty level.
     *
     * @return The number of shuffle moves.
     */
    protected int getShuffleMoves() {
        int baseMultiplier; // Base multiplier for shuffle moves
        int diffLevel = player.getDifficultyLevel();
        
        // Extensible difficulty system - base multiplier scales with difficulty level
        if (diffLevel <= 3) {
            // Original difficulty levels
            switch (diffLevel) {
                case 1: // Easy
                    baseMultiplier = 3;
                    break;
                case 2: // Medium
                    baseMultiplier = 10;
                    break;
                case 3: // Hard
                    baseMultiplier = 25;
                    break;
                default:
                    baseMultiplier = 3;
                    break;
            }
        } else {
            // Extended difficulty levels - exponential scaling
            baseMultiplier = (int) Math.pow(5, diffLevel - 1);
        }

        double exponent = 1.5; // Exponent to scale with grid size
        double gridSize = rows * cols;

        // Calculate shuffle moves using a custom formula
        return (int) (baseMultiplier * Math.pow(gridSize, exponent));
    }
    
    /**
     * Calculates the current score based on moves, time, difficulty, and grid size.
     * Higher difficulty and larger grids give more base points.
     * Fewer moves and less time result in higher scores.
     *
     * @return The calculated score
     */
    private int calculateScore() {
        if (moveCount == 0) return 0;
        
        long elapsedTimeSeconds = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsedTimeSeconds == 0) elapsedTimeSeconds = 1; // Avoid division by zero
        
        // Base score increases with difficulty and grid size
        int baseScore = player.getDifficultyLevel() * (rows * cols) * 100;
        
        // Efficiency bonus: fewer moves and less time = higher score
        double moveEfficiency = Math.max(0.1, 1.0 / moveCount);
        double timeEfficiency = Math.max(0.1, 1.0 / elapsedTimeSeconds);
        
        // Calculate final score
        return (int) (baseScore * moveEfficiency * timeEfficiency * 10);
    }

    /**
     * Returns a list of possible moves (row, col) for the empty cell.
     *
     * @return A list of possible moves.
     */
    private List<int[]> getPossibleMoves() {
        List<int[]> moves = new ArrayList<>();

        // Check all four possible directions (up, down, left, right)
        if (emptyRow > 0)
            moves.add(new int[] { emptyRow - 1, emptyCol }); // Up
        if (emptyRow < rows - 1)
            moves.add(new int[] { emptyRow + 1, emptyCol }); // Down
        if (emptyCol > 0)
            moves.add(new int[] { emptyRow, emptyCol - 1 }); // Left
        if (emptyCol < cols - 1)
            moves.add(new int[] { emptyRow, emptyCol + 1 }); // Right

        return moves;
    }

    /**
     * Constructor to initialize the sliding puzzle game.
     *
     * @param row The number of rows in the grid.
     * @param col The number of columns in the grid.
     */
    public SlidingPuzzleGame() {
        super(Integer.class, DEFAULT_ROWS, DEFAULT_COLS);
        
        // Add additional difficulty levels to demonstrate extensibility
        addDifficultyLevel(4, "Expert");
        addDifficultyLevel(5, "Master");
        addDifficultyLevel(6, "Legendary");
    }

    @Override
    protected void displayWelcomeMessage() {
        System.out.println("=========================================");
        System.out.println("    WELCOME TO THE SLIDING PUZZLE GAME!  ");
        System.out.println("=========================================");
        System.out.println("\n--- How to Play ---");
        System.out.println("1. Objective: Arrange the numbers in ascending order, from left to right, top to bottom.");
        System.out.println("   The empty space should be in the bottom-right corner when solved.");
        System.out.println("\n   For a 3x3 puzzle, the solved state looks like this:");
        System.out.println("   +--+--+--+");
        System.out.println("   | 1| 2| 3|");
        System.out.println("   +--+--+--+");
        System.out.println("   | 4| 5| 6|");
        System.out.println("   +--+--+--+");
        System.out.println("   | 7| 8|  |");
        System.out.println("   +--+--+--+");
        System.out.println("\n2. Your Move: To move a tile, enter the number of the tile you wish to slide");
        System.out.println("   into the empty space. You can only move tiles that are adjacent");
        System.out.println("   (up, down, left, or right) to the empty space.");
        System.out.println("\nGood luck and have fun! 🧩");
        System.out.println("-----------------------------------------\n");
    }

    @Override
    protected void makeMove(int row, int col) {
        // Swap the empty cell with the selected cell
        grid[emptyRow][emptyCol] = grid[row][col];
        grid[row][col] = 0; // 0 represents the empty cell

        // Update the position of the empty cell
        emptyRow = row;
        emptyCol = col;
        
        // Update score tracking
        moveCount++;
        currentScore = calculateScore();
    }

    @Override
    protected void initializeGame() {
        // Create a list of numbers from 1 to rows*cols-1
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i < rows * cols; i++) {
            numbers.add(i);
        }
        numbers.add(0); // 0 represents the empty cell

        // Fill the grid in a solved state with the empty cell at the bottom-right
        // corner
        Iterator<Integer> iterator = numbers.iterator();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = iterator.next();
            }
        }
        emptyRow = rows - 1;
        emptyCol = cols - 1;

        // Shuffle the grid by making valid moves from the solved state
        // This ensures the puzzle is always solvable
        // Number of random moves to shuffle the puzzle will depend on the difficulty level
        // and grid size
        int shuffleMoves = getShuffleMoves();
        for (int i = 0; i < shuffleMoves; i++) {
            List<int[]> possibleMoves = getPossibleMoves();

            // Choose a random move from the possible moves
            if (!possibleMoves.isEmpty()) {
                Random random = new Random();

                // Making a random move out of the possible moves
                int[] move = possibleMoves.get(random.nextInt(possibleMoves.size()));
                makeMove(move[0], move[1]);
            }
        }
        isGameOver = false; // Reset game over status
        
        // Initialize score tracking
        currentScore = 0;
        moveCount = 0;
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void processUserInput() {
        System.out.print(getPlayerInfo() + ", which tile do you want to slide to the empty space? ");
        String input = scanner.nextLine();
        try {
            int move_tile = Integer.parseInt(input);
            if (move_tile < 1 || move_tile > rows * cols - 1) {
                System.out.println("Invalid tile number. Please enter a number between 1 and " + (rows * cols - 1));
                return;
            }

            // Find the position of the tile
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (grid[i][j] == move_tile) {
                        // Check if the tile is adjacent to the empty cell
                        if ((Math.abs(emptyRow - i) == 1 && emptyCol == j) || // Up or Down
                                (Math.abs(emptyCol - j) == 1 && emptyRow == i)) { // Left or Right
                            makeMove(i, j);
                            return;
                        }
                    }
                }
            }

            // Handling invalid move (not adjacent)
            System.out.println("Invalid move. The tile must be adjacent to the empty space.");
        } catch (NumberFormatException e) {
            displayInvalidInputMessage();
        }
    }

    @Override
    protected boolean checkWinCondition() {
        // if the empty cell is not in the bottom-right corner, return false immediately
        if (grid[rows - 1][cols - 1] != 0) {
            return false;
        }

        // Check if the numbers are in ascending order
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Skip the empty cell
                if (i == rows - 1 && j == cols - 1) {
                    continue;
                }

                if (grid[i][j] != i * cols + j + 1) {
                    // If a number is out of place, the puzzle is not solved
                    return false;
                }
            }
        }

        // If all checks passed, the game is won
        return true;
    }

    @Override
    protected void displayInvalidInputMessage() {
        System.out.println("Invalid input. Please try again.");
        displayGrid(); // Re-display the grid
        processUserInput(); // Prompt for input again
    }

    @Override
    protected void displayWinMessage() {
        // clear the console
        System.out.print("\033[H\033[2J");
        System.out.flush();

        // Calculate final score
        int finalScore = calculateScore();
        long totalTime = (System.currentTimeMillis() - startTime) / 1000;
        
        // Display the final grid and a congratulatory message
        System.out.println("Congratulations " + getPlayerInfo() + "! You've solved the puzzle! 🎉");
        System.out.println();
        System.out.println("=== GAME STATISTICS ===");
        System.out.println("Final Score: " + finalScore);
        System.out.println("Total Moves: " + moveCount);
        System.out.println("Total Time: " + totalTime + " seconds");
        System.out.println("Difficulty: " + (player.getDifficultyLevel() == 1 ? "Easy" : 
                                          player.getDifficultyLevel() == 2 ? "Medium" : "Hard"));
        System.out.println("Grid Size: " + rows + "x" + cols);
        
        // Check and update top score
        int previousTopScore = player.getTopScore();
        boolean newRecord = player.updateTopScore(finalScore);
        if (newRecord) {
            System.out.println();
            System.out.println("🏆 NEW PERSONAL RECORD! 🏆");
            if (previousTopScore > 0) {
                System.out.println("Previous best: " + previousTopScore + " (improved by " + (finalScore - previousTopScore) + ")");
            } else {
                System.out.println("This is your first completed game at this difficulty!");
            }
        }
        
        // Display all top scores
        Map<Integer, Integer> allScores = player.getAllTopScores();
        System.out.println();
        System.out.println("=== YOUR TOP SCORES ===");
        if (allScores.isEmpty()) {
            System.out.println("No scores recorded yet.");
        } else {
            List<Integer> levels = new ArrayList<>(allScores.keySet());
            Collections.sort(levels);
            for (int level : levels) {
                System.out.println(getDifficultyName(level) + " (Level " + level + "): " + allScores.get(level));
            }
        }
        System.out.println("=======================");
        System.out.println();

        // Ask if the player wants to play again
        System.out.println("Would you like to play again? (yes/no)");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("yes") || response.equals("y")) {
            play(); // Restart the game
            return;
        }

        System.out.println("Thanks for playing the Sliding Puzzle Game. Goodbye!");
        scanner.close();
    }

    @Override
    protected void displayGrid() {
        StringBuilder sb = new StringBuilder();

        // Print the top border
        sb.append(topLeftCorner);
        for (int j = 0; j < cols; j++) {
            sb.append(horizontalBorder);
        }
        sb.append("\n");

        // Print each row of the grid
        for (int i = 0; i < rows; i++) {
            sb.append(verticalBorder);
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 0) {
                    sb.append(emptyCell).append(verticalBorder);
                } else {
                    sb.append(String.format(cellFormat, grid[i][j])).append(verticalBorder);
                }
            }

            // Print the row separator
            sb.append("\n" + topLeftCorner);

            for (int j = 0; j < cols; j++) {
                sb.append(horizontalBorder);
            }
            sb.append("\n");
        }

        System.out.println(sb.toString());
        
        // Display game statistics
        if (moveCount > 0) {
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            String currentDifficulty = getDifficultyName(player.getDifficultyLevel());
            System.out.println("Moves: " + moveCount + " | Time: " + elapsedTime + "s | Current Score: " + currentScore);
            System.out.println(player.getName() + "'s Top Score (" + currentDifficulty + "): " + player.getTopScore());
        }
    }
}

/* SlidingPuzzle.java */
public class SlidingPuzzle {
    // Main method to start the game
    public static void main(String[] args) {
        SlidingPuzzleGame game = new SlidingPuzzleGame();
        game.startGame();
    }
}