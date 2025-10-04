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

import java.util.List;
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

    protected abstract void validateSize();

    protected void setPlayerName() {
        System.out.print("Enter player name: ");
        String playerName = scanner.nextLine();

        if (playerName != null && !playerName.trim().isEmpty()) {
            this.player.setName(playerName.trim());
        } else {
            System.out.println("Warning: Invalid name provided. Setting default name.");
            this.player.setName(Player.getDefaultPlayerName());
        }
    }

    protected String getPlayerInfo() {
        return player.getName();
    }

    protected abstract void setSize();

    protected void setDifficultyLevel() {
        StringBuilder optionsBuilder = new StringBuilder();
        List<Integer> levels = difficultyManager.getSortedDifficultyLevels();
        if (levels.isEmpty()) {
            throw new IllegalStateException("No difficulty levels configured.");
        }

        for (int i = 0; i < levels.size(); i++) {
            int level = levels.get(i);
            optionsBuilder.append(level).append(" (").append(difficultyManager.getDifficultyName(level)).append(")");
            if (i < levels.size() - 1) {
                optionsBuilder.append(", ");
            }
        }

        System.out.println("Hey " + getPlayerInfo() + ", choose your difficulty level: " + optionsBuilder);
        System.out.println("Note: The difficulty level increases exponentially with grid size");
        String chosenLevel = scanner.nextLine();

        int defaultLevel = difficultyManager.getMinDifficultyLevel();
        int level;
        try {
            level = Integer.parseInt(chosenLevel);
        } catch (Exception e) {
            System.out.println("Invalid input. Defaulting to " + getDifficultyName(defaultLevel) + ".");
            level = defaultLevel;
        }

        player.setDifficultyLevel(level);

        if (!isValidDifficultyLevel(level)) {
            System.out.println("Invalid level. Defaulting to " + getDifficultyName(defaultLevel) + ".");
            player.setDifficultyLevel(defaultLevel);
        }

        System.out.println("Difficulty set to: " + getDifficultyName(player.getDifficultyLevel()));

        int currentTopScore = player.getTopScore(getRows(), getCols());
        if (currentTopScore > 0) {
            System.out.println("Your current top score for this difficulty: " + currentTopScore);
        } else {
            System.out.println("No previous score for this difficulty level.");
        }

        showDifficultySpecificMessage(player.getDifficultyLevel());
    }

    protected void showDifficultySpecificMessage(int difficultyLevel) {
        if (difficultyLevel >= 4) {
            System.out.println("🔥 EXTREME DIFFICULTY ACTIVATED! 🔥");
            System.out.println(
                    "Warning: " + getDifficultyName(difficultyLevel) + " mode is for seasoned puzzle masters!");
            System.out.println(
                    "Tip: Take your time and think several moves ahead. Consider using pen and paper to track your strategy.");
        } else if (difficultyLevel == 3) {
            System.out.println("Warning: " + getDifficultyName(difficultyLevel) + " mode can be quite challenging!");
            System.out.println("Tip: Plan your moves ahead and try to visualize the solution.");
        } else if (difficultyLevel == 1) {
            System.out.println("Perfect for beginners! Take your time to learn the game mechanics.");
        }
    }

    protected abstract void displayWelcomeMessage();

    protected abstract void initializeGame();

    protected abstract void displayGrid();

    protected abstract void processUserInput();

    protected abstract void makeMove(int row, int col);

    protected abstract void displayInvalidInputMessage();

    protected abstract boolean checkWinCondition();

    protected abstract void displayWinMessage();
}
