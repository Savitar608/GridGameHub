import java.util.*;
import java.lang.StringBuilder;
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
    protected String playerName;
    protected int difficultyLevel;

    private static final String DEFAULT_PLAYER_NAME = "Master Chief";
    private static final int DEFAULT_DIFFICULTY_LEVEL = 1; // Easy

    private static final Map<Integer, String> difficultyLevels;
    static {
        Map<Integer, String> temp = new HashMap<>();
        temp.put(1, "Easy");
        temp.put(2, "Medium");
        temp.put(3, "Hard");
        difficultyLevels = Collections.unmodifiableMap(temp);
    }

    /**
     * Constructor to initialize the game.
     *
     * @param rows The number of rows in the grid.
     * @param cols The number of columns in the grid.
     */
    public GridGame(Class<T> clazz, int rows, int cols) {
        if (rows < 1 || cols < 1) {
            throw new IllegalArgumentException("Row and column sizes must be positive integers.");
        }

        @SuppressWarnings("unchecked")
        // Create a 2D generic array properly using reflection
        // Spent a lot of time figuring this out
        T[][] tempGrid = (T[][]) Array.newInstance(clazz, rows, 0);
        for (int i = 0; i < rows; i++) {
            tempGrid[i] = (T[]) Array.newInstance(clazz, cols);
        }

        // Initialize the grid and game state
        this.rows = rows;
        this.cols = cols;
        this.grid = tempGrid;
        this.isGameOver = false;
        this.scanner = new Scanner(System.in);
        this.playerName = DEFAULT_PLAYER_NAME; // Can be set later if needed. Setting a default name.
        this.difficultyLevel = DEFAULT_DIFFICULTY_LEVEL; // Default difficulty level
    }


    /**
     * Starts the game by displaying the welcome message and setting the player name.
     */
    public void startGame() {
        this.isGameOver = false; // Reset game over status
        displayWelcomeMessage();
        setPlayerName();

        play();
    }

    /**
     * The main public method to start and run the game.
     */
    private void play() {
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
            this.playerName = playerName.trim();
        } else {
            // setting a default name and displaying a warning
            System.out.println("Warning: Invalid name provided. Setting default name.");
            this.playerName = DEFAULT_PLAYER_NAME; // Default name
        }
    }

    /**
     * Returns the player's name for display purposes.
     *
     * @return the player's name.
     */
    protected String getPlayerInfo() {
        // Return the player's name or a default if not set
        return playerName != null ? playerName : DEFAULT_PLAYER_NAME;
    }

    /**
     * Sets the size of the grid.
     */
    protected void setSize() {
        System.out.print("Enter grid size (rows cols): ");
        String sizeInput = scanner.nextLine();
        String[] parts = sizeInput.trim().split("\\s+");

        if (parts.length == 2) {
            try {
                int rows = Integer.parseInt(parts[0]);
                int cols = Integer.parseInt(parts[1]);

                // if the size is the default size, skip reinitialization
                if (rows == this.rows && cols == this.cols) {
                    return;
                }
                
                // Set the new size
                this.rows = rows;
                this.cols = cols;

                // Reinitialize the grid with the new size using proper generic array creation
                @SuppressWarnings("unchecked")
                T[][] tempGrid = (T[][]) Array.newInstance(Integer.class, rows, 0);
                for (int i = 0; i < rows; i++) {
                    tempGrid[i] = (T[]) Array.newInstance(Integer.class, cols);
                }
                this.grid = tempGrid;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Using default size.");
            }
        } else {
            System.out.println("Invalid input. Using default size.");
        }
        // Validate the size
        validateSize();
    }
    /**
     * Sets the difficulty level of the game.
     *
     * @param level The difficulty level (1 to 3).
     */
    protected void setDifficultyLevel() {
        System.out.println("Choose difficulty level: 1 (Easy), 2 (Medium), 3 (Hard)");
        String chosenLevel = scanner.nextLine();

        try {
            // Parse the input to an integer
            int level = Integer.parseInt(chosenLevel);
            this.difficultyLevel = level;
        } catch (Exception e) {
            // If parsing fails, default to Easy
            System.out.println("Invalid input. Defaulting to Easy.");
            this.difficultyLevel = DEFAULT_DIFFICULTY_LEVEL;
        }

        // Validate the level
        // If invalid, default to Easy
        if (this.difficultyLevel < 1 || this.difficultyLevel > 3) {
            System.out.println("Invalid level. Defaulting to Easy.");
            this.difficultyLevel = DEFAULT_DIFFICULTY_LEVEL;
        }

        System.out.println("Difficulty set to: " + difficultyLevels.get(difficultyLevel));
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
    public static final int MIN_SIZE = 2;
    private static final String EMPTY_CELL = "  ";

    // Position of the empty cell
    private int emptyRow;
    private int emptyCol;

    // Default values
    private static final int DEFAULT_ROWS = 3; // Default rows
    private static final int DEFAULT_COLS = 3; // Default columns

    /**
     * Validates the size of the grid.
     */
    protected void validateSize() {
        if (rows < MIN_SIZE || cols < MIN_SIZE) {
            throw new IllegalArgumentException("Grid size must be at least " + MIN_SIZE + "x" + MIN_SIZE);
        }
    }

    /**
     * Determines the number of shuffle moves based on the difficulty level.
     *
     * @return The number of shuffle moves.
     */
    protected int getShuffleMoves() {
        switch (difficultyLevel) {
            case 1: // Easy
                return 300;
            case 2: // Medium
                return 1000;
            case 3: // Hard
                return 5000;
            default:
                return 300; // Default to Easy if something goes wrong
        }
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
        // Number of random moves to shuffle the puzzle will depend on the difficulty
        // level
        // Easy: 10 moves, Medium: 100 moves, Hard: 500 moves
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

        // Display the final grid and a congratulatory message
        System.out.println("Congratulations " + getPlayerInfo() + "! You've solved the puzzle! 🎉");
        displayGrid();

        // Ask if the player wants to play again
        System.out.println("Would you like to play again? (yes/no)");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("yes") || response.equals("y")) {
            play(); // Restart the game
        }

        System.out.println("Thanks for playing the Sliding Puzzle Game. Goodbye!");
        scanner.close();
    }


    @Override
    protected void displayGrid() {
        StringBuilder sb = new StringBuilder();
        
        // Print the top border
        sb.append("+");
        for (int j = 0; j < cols; j++) {
            sb.append("--+");
        }
        sb.append("\n");

        // Print each row of the grid
        for (int i = 0; i < rows; i++) {
            sb.append("|");
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 0) {
                    sb.append(EMPTY_CELL).append("|");
                } else {
                    sb.append(String.format("%2d", grid[i][j])).append("|");
                }
            }

            // Print the row separator
            sb.append("\n+");
            
            for (int j = 0; j < cols; j++) {
                sb.append("--+");
            }
            sb.append("\n");
        }

        System.out.println(sb.toString());
    }
}

/* SlidingPuzzle.java */
public class SlidingPuzzle {
    // Main method to start the game
    public static void main(String[] args) {
        SlidingPuzzleGame game = new SlidingPuzzleGame();
        game.play();
    }
}