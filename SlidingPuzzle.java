import java.util.*;

/**
 * An abstract base class for grid-based terminal games.
 * It provides the foundational structure for a game board, game state,
 * and the main game loop.
 * It can be extended to create games such as a sliding puzzle game
 *
 * @param <T> The type of elements stored in the grid.
 */
abstract class GridGame<T> {
    protected T[][] grid;
    protected boolean isGameOver;
    protected Scanner scanner;
    protected String playerName;

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
        this.grid = tempGrid;
        this.isGameOver = false;
        this.scanner = new Scanner(System.in);
        this.playerName = "Master Chief"; // Can be set later if needed. Setting a default name.
    }

    /**
     * The main public method to start and run the game.
     */
    public void play() {
        initializeGame();
        displayWelcomeMessage();
        

        // Looping over the game until it's over
        while (!isGameOver) {
            displayGrid();
            processUserInput();
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

    protected void setPlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.playerName = name.trim();
        } else {
            // setting a default name and displaying a warning
            System.out.println("Warning: Invalid name provided. Setting default name.");
            this.playerName = "Gamer-X";
        }
    }

    /**
     * Returns the player's name for display purposes.
     *
     * @return the player's name.
     */
    protected String getPlayerInfo() {
        return playerName != null ? playerName : "Player";
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
     * Checks if the player move is valid.
     *
     * @return true if the move is valid, false otherwise.
     */
    protected abstract boolean isValidMove();

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
    protected void initializeGame() {
        int size = grid.length;

        // generate a random number between 1 and size*size
        Random random = new Random();
        int randomIndex = random.nextInt(size * size) + 1;

        // Create a list of numbers from 1 to size*size
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= size * size; i++) {
            if (i != randomIndex) {
                numbers.add(i);
            } else {
            // Leave one space for the empty cell
            // Using 0 to represent the empty cell
                numbers.add(0);
            }
        }

        // Shuffle the numbers and fill the grid
        Collections.shuffle(numbers);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int number = numbers.get(i * size + j);
                if (number != 0) {
                    grid[i][j] = number;
                } else {
                    grid[i][j] = null; // Representing the empty cell with null
                }
            }
        }
    }
}