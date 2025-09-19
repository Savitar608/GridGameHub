import java.util.*;
import java.lang.StringBuilder;

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
        // Java does not allow the direct creation of generic arrays apparently
        T[][] tempGrid = (T[][]) java.lang.reflect.Array.newInstance(clazz, rows, cols);

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
     * The main public method to start and run the game.
     */
    public void play() {
        initializeGame();
        displayWelcomeMessage();
        setPlayerName();
        setDifficultyLevel();

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
        scanner.close();
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

public class SlidingPuzzle extends GridGame<Integer> {
    public static final int MIN_SIZE = 2;
    private static final String EMPTY_CELL = "  ";

    private int emptyRow;
    private int emptyCol;

    /**
     * Validates the size of the grid.
     */
    protected void validateSize(int row, int col) {
        if (row < MIN_SIZE || col < MIN_SIZE) {
            throw new IllegalArgumentException("Grid size must be at least " + MIN_SIZE + "x" + MIN_SIZE);
        }
        if (row != col) {
            throw new IllegalArgumentException("Grid must be square (rows must equal columns)");
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
                return 10;
            case 2: // Medium
                return 100;
            case 3: // Hard
                return 500;
            default:
                return 10; // Default to Easy if something goes wrong
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
    public SlidingPuzzle(int row, int col) {
        super(Integer.class, row, col);

        validateSize(row, col);
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
        // Create a list of numbers from 0 to rows*cols-1
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < rows * cols; i++) {
            numbers.add(i);
        }

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
    }

    @Override
    protected void processUserInput() {
        System.out.println(getPlayerInfo() + ", which tile do you want to slide to the empty space? ");
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
            System.out.println("Invalid input. Please enter a valid tile number.");
            scanner.nextLine(); // Clear the invalid input
            displayGrid();
            processUserInput();
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
                if (grid[i][j] != i * cols + j + 1) {
                    return false;
                }
            }
        }

        // If all checks passed, the game is won
        return true;
    }
}