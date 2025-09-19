import java.util.*;
import java.io.*;
import java.math.*;

/**
 * An abstract base class for grid-based terminal games.
 * It provides the foundational structure for a game board, game state,
 * and the main game loop.
 * It can be extended to create games such as a sliding puzzle game
 *
 * @param <T> The type of elements stored in the grid.
 */
public abstract class GridGame<T> {

    protected T[][] grid;
    protected boolean isGameOver;
    protected Scanner scanner;

    /**
     * Constructor to initialize the game.
     *
     * @param rows The number of rows in the grid.
     * @param cols The number of columns in the grid.
     */
    public GridGame(int rows, int cols) {
        this.grid = (T[][]) new Object[rows][cols];
        this.isGameOver = false;
        this.scanner = new Scanner(System.in);
    }

    /**
     * The main public method to start and run the game.
     */
    public void play() {
        initializeGame();
        displayWelcomeMessage();

        // Looping over the
        while (!isGameOver) {
            displayGrid();
            processUserInput();
            updateGameState();
            isGameOver = checkWinCondition();
        }

        displayGrid();
        displayWinMessage();
        scanner.close();
    }

    /*--- Abstract Methods to be Implemented by Child Classes ---*/

    /**
     * Sets up the initial state of the grid and game variables.
     */
    protected abstract void initializeGame();

    /**
     * Prints the game's welcome message and rules.
     */
    protected abstract void displayWelcomeMessage();

    /**
     * Renders the current state of the grid to the console.
     */
    protected abstract void displayGrid();

    /**
     * Waits for and handles input from the player.
     */
    protected abstract void processUserInput();

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