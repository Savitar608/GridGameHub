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
    public SlidingPuzzle(int size) {
        super(Integer.class, size, size);
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
}