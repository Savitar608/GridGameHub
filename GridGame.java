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

import java.util.Objects;

/**
 * An abstract base class for grid-based terminal games.
 *
 * @param <T> The type of elements stored in the grid.
 */
public abstract class GridGame<T> {
    protected Grid<T> gameGrid;
    protected boolean isGameOver;
    protected Player player;
    protected final DifficultyManager difficultyManager;
    private final InputService inputService;
    private final OutputService outputService;
    private final GameController gameController;
        private boolean exitRequested;

        protected static final String QUIT_KEYWORD = "quit";

    /**
     * Constructor to initialize the game.
     *
     * @param componentType The class type of grid elements
     * @param rows          The number of rows in the grid.
     * @param cols          The number of columns in the grid.
     */
    public GridGame(Class<T> componentType, int rows, int cols) {
        this(componentType, rows, cols, new ConsoleInputService(), new ConsoleOutputService());
    }

    /**
     * Constructs a grid game that uses the provided input and output services.
     *
     * @param componentType  the element type stored within the grid
     * @param rows           total rows in the grid
     * @param cols           total columns in the grid
     * @param inputService   input provider used during gameplay
     * @param outputService  output destination used during gameplay
     */
    protected GridGame(Class<T> componentType, int rows, int cols,
        InputService inputService,
        OutputService outputService) {
        this.gameGrid = new Grid<>(componentType, rows, cols);
        this.isGameOver = false;
        this.player = new Player();
        this.difficultyManager = new DifficultyManager();
        this.inputService = Objects.requireNonNull(inputService, "inputService must not be null");
        this.outputService = Objects.requireNonNull(outputService, "outputService must not be null");

        initializeDefaultDifficultyLevels();
        this.gameController = new GameController();
        this.exitRequested = false;
    }

    /**
     * Starts the game by delegating to the internal controller.
     */
    public void startGame() {
        gameController.run(this);
    }

    /**
     * Retrieves the number of rows currently configured for the grid.
     *
     * @return row count of the grid
     */
    protected int getRows() {
        return gameGrid.getRows();
    }

    /**
     * Retrieves the number of columns currently configured for the grid.
     *
     * @return column count of the grid
     */
    protected int getCols() {
        return gameGrid.getCols();
    }

    /**
     * Provides the total number of cells in the grid.
     *
     * @return total cell count (rows * cols)
     */
    protected int getGridSize() {
        return gameGrid.getSize();
    }

    /**
     * Initializes the default difficulty levels. Can be overridden by subclasses.
     */
    /**
     * Defines baseline difficulty levels shared across grid-based games. Games can
     * override this to supply their own presets.
     */
    protected void initializeDefaultDifficultyLevels() {
        difficultyManager.clear();
        difficultyManager.addDifficultyLevel(1, "Easy");
        difficultyManager.addDifficultyLevel(2, "Medium");
        difficultyManager.addDifficultyLevel(3, "Hard");
    }

    /**
     * Registers a new difficulty level with the underlying manager.
     *
     * @param level numeric identifier for the difficulty
     * @param name  human-readable label presented to the player
     */
    protected void addDifficultyLevel(int level, String name) {
        difficultyManager.addDifficultyLevel(level, name);
    }

    /**
     * Looks up the configured display name for the supplied difficulty level.
     *
     * @param level difficulty identifier
     * @return readable label associated with the level
     */
    protected String getDifficultyName(int level) {
        return difficultyManager.getDifficultyName(level);
    }

    /**
     * Checks whether the specified difficulty level exists in the registry.
     *
     * @param level difficulty identifier to query
     * @return {@code true} if a matching level is configured; otherwise {@code false}
     */
    protected boolean isValidDifficultyLevel(int level) {
        return difficultyManager.isValidDifficultyLevel(level);
    }

    /**
     * Resets transient game metadata to prepare for a new round.
     */
    public void resetGameState() {
        this.isGameOver = false;
    }

    /**
     * Updates the game-over flag.
     *
     * @param gameOver {@code true} to mark the game as complete
     */
    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
    }

    /**
     * Indicates whether the current game has concluded.
     *
     * @return {@code true} when the game has ended
     */
    public boolean isGameOver() {
        return isGameOver;
    }

        public boolean isExitRequested() {
            return exitRequested;
        }

        protected void requestExit() {
            this.exitRequested = true;
            this.isGameOver = true;
        }

        protected boolean isQuitCommand(String input) {
            return input != null && QUIT_KEYWORD.equalsIgnoreCase(input.trim());
        }

    /**
     * Provides access to the configured input service.
     *
     * @return active input service
     */
    public InputService getInputService() {
        return inputService;
    }

    /**
     * Provides access to the configured output service.
     *
     * @return active output service
     */
    public OutputService getOutputService() {
        return outputService;
    }

    /**
     * Returns the player metadata associated with this game instance.
     *
     * @return player configuration
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Exposes the difficulty manager, allowing subclasses to customize levels.
     *
     * @return shared difficulty manager
     */
    public DifficultyManager getDifficultyManager() {
        return difficultyManager;
    }

    /**
     * Verifies that the configured grid size remains within valid bounds for the
     * concrete game implementation.
     */
    protected abstract void validateSize();

    /**
     * Prompts for and applies the grid dimensions used for the next round.
     */
    protected abstract void setSize();

    /**
     * Displays an introduction to the player explaining how to play the game.
     */
    protected abstract void displayWelcomeMessage();

    /**
     * Populates the grid and any ancillary state ahead of gameplay.
     */
    protected abstract void initializeGame();

    /**
     * Renders the current game board state to the player.
     */
    protected abstract void displayGrid();

    /**
     * Processes a single unit of player input within the game loop.
     */
    protected abstract void processUserInput();

    /**
     * Executes the low-level move logic for the concrete game implementation.
     *
     * @param row row index of the piece to move
     * @param col column index of the piece to move
     */
    protected abstract void makeMove(int row, int col);

    /**
     * Informs the player that their input could not be processed.
     */
    protected abstract void displayInvalidInputMessage();

    /**
     * Determines whether the player has satisfied the game's win condition.
     *
     * @return {@code true} when the puzzle is solved
     */
    protected abstract boolean checkWinCondition();

    /**
     * Presents the victory screen and end-of-game summary to the player.
     */
    protected abstract void displayWinMessage();
}
