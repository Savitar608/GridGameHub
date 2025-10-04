/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 * 
 * File: GridGame.java
 * Description: Abstract foundation for grid-based terminal games. Provides shared
 *              functionality for managing the grid, player metadata, difficulty
 *              levels, and the overall game loop orchestration through the
 *              {@link GameController}.
 * 
 * Features:
 * - Shared grid access helpers for subclasses
 * - Difficulty management via {@link DifficultyManager}
 * - Player information capture and validation
 * - Delegated game loop coordination through {@link GameController}
 * 
 * @version 2.0
 * @date October 4, 2025
 * @course CS611 - Object Oriented Design
 * @assignment Assignment 1
 */

import java.util.Scanner;

/**
 * An abstract base class for grid-based terminal games.
 *
 * @param <T> The type of elements stored in the grid.
 */
public abstract class GridGame<T> {
    protected Grid<T> gameGrid;
    protected boolean isGameOver;
    protected Scanner scanner;
    protected Player player;
    protected final DifficultyManager difficultyManager;
    private final GameController gameController;

    /**
     * Constructor to initialize the game.
     *
     * @param componentType The class type of grid elements
     * @param rows          The number of rows in the grid.
     * @param cols          The number of columns in the grid.
     */
    public GridGame(Class<T> componentType, int rows, int cols) {
        this.gameGrid = new Grid<>(componentType, rows, cols);
        this.isGameOver = false;
        this.scanner = new Scanner(System.in);
        this.player = new Player();
        this.difficultyManager = new DifficultyManager();

        initializeDefaultDifficultyLevels();
        this.gameController = new GameController();
    }

    /**
     * Starts the game by delegating to the internal controller.
     */
    public void startGame() {
        gameController.run(this);
    }

    protected int getRows() {
        return gameGrid.getRows();
    }

    protected int getCols() {
        return gameGrid.getCols();
    }

    protected int getGridSize() {
        return gameGrid.getSize();
    }

    /**
     * Initializes the default difficulty levels. Can be overridden by subclasses.
     */
    protected void initializeDefaultDifficultyLevels() {
        difficultyManager.clear();
        difficultyManager.addDifficultyLevel(1, "Easy");
        difficultyManager.addDifficultyLevel(2, "Medium");
        difficultyManager.addDifficultyLevel(3, "Hard");
    }

    protected void addDifficultyLevel(int level, String name) {
        difficultyManager.addDifficultyLevel(level, name);
    }

    protected String getDifficultyName(int level) {
        return difficultyManager.getDifficultyName(level);
    }

    protected boolean isValidDifficultyLevel(int level) {
        return difficultyManager.isValidDifficultyLevel(level);
    }

    public void resetGameState() {
        this.isGameOver = false;
    }

    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    protected Scanner getInputScanner() {
        return scanner;
    }

    public Player getPlayer() {
        return player;
    }

    public DifficultyManager getDifficultyManager() {
        return difficultyManager;
    }

    protected abstract void validateSize();

    protected abstract void setSize();

    protected abstract void displayWelcomeMessage();

    protected abstract void initializeGame();

    protected abstract void displayGrid();

    protected abstract void processUserInput();

    protected abstract void makeMove(int row, int col);

    protected abstract void displayInvalidInputMessage();

    protected abstract boolean checkWinCondition();

    protected abstract void displayWinMessage();
}
