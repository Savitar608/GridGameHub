
/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 * 
 * File: SlidingPuzzleGame.java
 * Description: Concrete implementation of a sliding puzzle built atop {@link GridGame}.
 *              Handles puzzle initialization, shuffling, user interactions, scoring,
 *              and win detection across extensible difficulty levels and grid sizes.
 * 
 * Features:
 * - Configurable grid sizes ranging from 3x3 to 20x20
 * - Difficulty-aware shuffling and score calculations
 * - Solvability maintained by performing randomized valid moves from the solved state
 * - Per-grid, per-difficulty score tracking via {@link Player}
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


/**
 * Sliding puzzle game implementation extending the generic {@link GridGame} framework.
 * Provides specific functionality for the sliding puzzle mechanics.
 * - Customizable grid initialization and shuffling
 * - Move validation and tracking
 * - Score calculation based on moves, time, difficulty, and grid size
 * - Contextual messaging based on selected difficulty
 */
public final class SlidingPuzzleGame extends GridGame<SlidingPuzzlePiece> {
    public static final int MIN_SIZE = 3;
    public static final int MAX_SIZE = 20;

    private int emptyRow;
    private int emptyCol;

    private int currentScore;
    private int moveCount;
    private long startTime;

    private static final int DEFAULT_ROWS = 3;
    private static final int DEFAULT_COLS = 3;

    private String emptyCell = "  ";
    private String topLeftCorner = "+";
    private String horizontalBorder = "--+";
    private String verticalBorder = "|";
    private String cellFormat = "%2s";

    /**
     * Creates a sliding puzzle game using the default console input/output
     * services.
     */
    public SlidingPuzzleGame() {
        super(SlidingPuzzlePiece.class, DEFAULT_ROWS, DEFAULT_COLS);
        addDifficultyLevel(4, "Expert");
        addDifficultyLevel(5, "Master");
        addDifficultyLevel(6, "Legendary");
    }

    /**
     * Creates a sliding puzzle game with custom input/output services (useful for
     * testing).
     *
     * @param inputService  service used for reading player input
     * @param outputService service used for writing game output
     */
    public SlidingPuzzleGame(InputService inputService, OutputService outputService) {
        super(SlidingPuzzlePiece.class, DEFAULT_ROWS, DEFAULT_COLS, inputService, outputService);
        addDifficultyLevel(4, "Expert");
        addDifficultyLevel(5, "Master");
        addDifficultyLevel(6, "Legendary");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ensures the selected grid dimensions remain within the supported range for
     * the sliding puzzle.
     */
    @Override
    protected void validateSize() {
        if (getRows() < MIN_SIZE || getCols() < MIN_SIZE) {
            throw new IllegalArgumentException("Grid size must be at least " + MIN_SIZE + "x" + MIN_SIZE);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Captures user input for grid dimensions, applying defaults and adjusting
     * display formatting based on the chosen size.
     */
    @Override
    protected void setSize() {
        OutputService outputService = getOutputService();
        InputService inputService = getInputService();

        outputService.print("Enter grid size (rows x cols) (Min " + MIN_SIZE + ", Max " + MAX_SIZE
                + ") or type 'quit' to exit: ");
        String sizeInput = inputService.readLine();
        if (sizeInput == null || isQuitCommand(sizeInput)) {
            requestExit();
            return;
        }
        String[] parts = sizeInput.trim().split("\\s*x\\s*");

        if (parts.length == 2) {
            try {
                int rows = Integer.parseInt(parts[0]);
                int cols = Integer.parseInt(parts[1]);

                if (rows == getRows() && cols == getCols()) {
                    return;
                }

                if (rows < MIN_SIZE || cols < MIN_SIZE || rows > MAX_SIZE || cols > MAX_SIZE) {
                    outputService.println(
                            "Invalid size. Using default size of " + DEFAULT_ROWS + "x" + DEFAULT_COLS + ".");
                    rows = DEFAULT_ROWS;
                    cols = DEFAULT_COLS;
                }

                gameGrid.resize(rows, cols);

                if (getGridSize() < 100) {
                    horizontalBorder = "--+";
                    emptyCell = "  ";
                    cellFormat = "%2s";
                } else {
                    horizontalBorder = "---+";
                    emptyCell = "   ";
                    cellFormat = "%3s";
                }
            } catch (NumberFormatException e) {
                outputService.println(
                        "Invalid input. Using default size of " + DEFAULT_ROWS + "x" + DEFAULT_COLS + ".");
            }
        } else {
            outputService.println(
                    "Invalid input. Using default size of " + DEFAULT_ROWS + "x" + DEFAULT_COLS + ".");
        }
        validateSize();
    }

    /**
     * Calculates how many randomized moves should be performed to shuffle the
     * puzzle before each game based on difficulty and grid size.
     *
     * @return number of shuffling iterations to execute
     */
    private int getShuffleMoves() {
        int baseMultiplier;
        int diffLevel = getPlayer().getDifficultyLevel();

        if (diffLevel <= 3) {
            switch (diffLevel) {
                case 1:
                    baseMultiplier = 3;
                    break;
                case 2:
                    baseMultiplier = 10;
                    break;
                case 3:
                    baseMultiplier = 25;
                    break;
                default:
                    baseMultiplier = 3;
                    break;
            }
        } else {
            baseMultiplier = (int) Math.pow(5, diffLevel - 1);
        }

        double exponent = 1.5;
        double gridSize = getGridSize();

        return (int) (baseMultiplier * Math.pow(gridSize, exponent));
    }

    /**
     * Computes the current score using difficulty, moves taken, and elapsed time
     * as factors.
     *
     * @return up-to-date score for the player
     */
    private int calculateScore() {
        if (moveCount == 0) {
            return 0;
        }

        long elapsedTimeSeconds = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsedTimeSeconds == 0) {
            elapsedTimeSeconds = 1;
        }

        // Base score scales with difficulty and grid size
        int baseScore = getPlayer().getDifficultyLevel() * getGridSize() * 100;

        // Apply penalties for moves and time taken
        double moveEfficiency = Math.max(0.1, 1.0 / moveCount);
        double timeEfficiency = Math.max(0.1, 1.0 / elapsedTimeSeconds);

        return (int) (baseScore * moveEfficiency * timeEfficiency * 10);
    }

    /**
     * Determines the set of board coordinates that are adjacent to the empty
     * tile and therefore movable.
     *
     * @return list of row/column index pairs eligible to slide into the gap
     */
    private List<int[]> getPossibleMoves() {
        List<int[]> moves = new ArrayList<>();

        if (emptyRow > 0) {
            moves.add(new int[] { emptyRow - 1, emptyCol });
        }
        if (emptyRow < getRows() - 1) {
            moves.add(new int[] { emptyRow + 1, emptyCol });
        }
        if (emptyCol > 0) {
            moves.add(new int[] { emptyRow, emptyCol - 1 });
        }
        if (emptyCol < getCols() - 1) {
            moves.add(new int[] { emptyRow, emptyCol + 1 });
        }

        return moves;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Supplements the generic welcome text with the sliding puzzle's specific
     * instructions and an illustrative solved board layout.
     */
    @Override
    protected void displayWelcomeMessage() {
        OutputService outputService = getOutputService();
        outputService.println("=========================================");
        outputService.println("    WELCOME TO THE SLIDING PUZZLE GAME!  ");
        outputService.println("=========================================");
        outputService.println("Type 'quit' at any prompt to exit the game.");
        outputService.println("\n--- How to Play ---");
        outputService
                .println("1. Objective: Arrange the numbers in ascending order, from left to right, top to bottom.");
        outputService.println("   The empty space should be in the bottom-right corner when solved.");
        outputService.println("\n   For a 3x3 puzzle, the solved state looks like this:");
        outputService.println("   +--+--+--+");
        outputService.println("   | 1| 2| 3|");
        outputService.println("   +--+--+--+");
        outputService.println("   | 4| 5| 6|");
        outputService.println("   +--+--+--+");
        outputService.println("   | 7| 8|  |");
        outputService.println("   +--+--+--+");
        outputService.println("\n2. Your Move: To move a tile, enter the number of the tile you wish to slide");
        outputService.println("   into the empty space. You can only move tiles that are adjacent");
        outputService.println("   (up, down, left, or right) to the empty space.");
        outputService.println("\nGood luck and have fun! 🧩");
        outputService.println("-----------------------------------------\n");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Moves the selected {@link SlidingPuzzlePiece} into the empty slot while
     * tracking the new location of that empty tile for future moves.
     */
    @Override
    protected void makeMove(int row, int col) {
        Tile<SlidingPuzzlePiece> sourceTile = gameGrid.getTile(row, col);
        SlidingPuzzlePiece tilePiece = sourceTile.getOccupant();
        if (tilePiece == null || tilePiece.isEmpty()) {
            return;
        }

        Tile<SlidingPuzzlePiece> emptyTile = gameGrid.getTile(emptyRow, emptyCol);
        emptyTile.setOccupant(tilePiece);
        sourceTile.setOccupant(SlidingPuzzlePiece.empty());

        emptyRow = row;
        emptyCol = col;

        moveCount++;
        currentScore = calculateScore();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Builds the solved board configuration and then performs a series of valid
     * random moves to produce a solvable shuffled state.
     */
    @Override
    protected void initializeGame() {
        List<SlidingPuzzlePiece> pieces = new ArrayList<>(getGridSize());
        for (int i = 1; i < getGridSize(); i++) {
            pieces.add(SlidingPuzzlePiece.ofValue(i));
        }
        pieces.add(SlidingPuzzlePiece.empty());

        gameGrid.fillFromList(pieces);
        emptyRow = getRows() - 1;
        emptyCol = getCols() - 1;

        int shuffleMoves = getShuffleMoves();
        Random random = new Random();
        for (int i = 0; i < shuffleMoves; i++) {
            List<int[]> possibleMoves = getPossibleMoves();
            if (!possibleMoves.isEmpty()) {
                int[] move = possibleMoves.get(random.nextInt(possibleMoves.size()));
                makeMove(move[0], move[1]);
            }
        }
        isGameOver = false;

        currentScore = 0;
        moveCount = 0;
        startTime = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Accepts numbered tile input, validates adjacency against the empty tile,
     * and executes the move or reports a descriptive error.
     */
    @Override
    protected void processUserInput() {
        OutputService outputService = getOutputService();
        InputService inputService = getInputService();

        outputService.print(getPlayer().getName()
                + ", which tile do you want to slide to the empty space? (type 'quit' to exit) ");
        String input = inputService.readLine();
        if (input == null || isQuitCommand(input)) {
            requestExit();
            return;
        }
        try {
            int moveTile = Integer.parseInt(input);
            if (moveTile < 1 || moveTile > getGridSize() - 1) {
                outputService.println(
                        "Invalid tile number. Please enter a number between 1 and " + (getGridSize() - 1));
                return;
            }

            for (int i = 0; i < getRows(); i++) {
                for (int j = 0; j < getCols(); j++) {
                    SlidingPuzzlePiece candidate = gameGrid.getPiece(i, j);
                    if (candidate != null && candidate.hasValue(moveTile)) {
                        if ((Math.abs(emptyRow - i) == 1 && emptyCol == j) ||
                                (Math.abs(emptyCol - j) == 1 && emptyRow == i)) {
                            makeMove(i, j);
                            return;
                        }
                    }
                }
            }

            outputService.println("Invalid move. The tile must be adjacent to the empty space.");
        } catch (NumberFormatException e) {
            displayInvalidInputMessage();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Validates that tiles appear in ascending order with the empty tile
     * occupying the lower-right corner of the grid.
     */
    @Override
    protected boolean checkWinCondition() {
        SlidingPuzzlePiece bottomRight = gameGrid.getPiece(getRows() - 1, getCols() - 1);
        if (bottomRight == null || !bottomRight.isEmpty()) {
            return false;
        }

        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                if (i == getRows() - 1 && j == getCols() - 1) {
                    continue;
                }

                SlidingPuzzlePiece expected = gameGrid.getPiece(i, j);
                if (expected == null || !expected.hasValue(i * getCols() + j + 1)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Provides the player immediate feedback on invalid input attempts before
     * giving them another chance to move.
     */
    @Override
    protected void displayInvalidInputMessage() {
        OutputService outputService = getOutputService();
        outputService.println("Invalid input. Please try again.");
        if (isExitRequested()) {
            return;
        }
        displayGrid();
        processUserInput();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Summarizes performance metrics, updates personal records, and surfaces the
     * player's historical leaderboard for the current difficulty.
     */
    @Override
    protected void displayWinMessage() {
        // Clear the console (works in most terminals)
        OutputService outputService = getOutputService();
        outputService.print("\033[H\033[2J");
        outputService.flush();

        int finalScore = calculateScore();
        long totalTime = (System.currentTimeMillis() - startTime) / 1000;

        outputService.println("Congratulations " + getPlayer().getName() + "! You've solved the puzzle! 🎉");
        outputService.println("");
        outputService.println("=== GAME STATISTICS ===");
        outputService.println("Final Score: " + finalScore);
        outputService.println("Total Moves: " + moveCount);
        outputService.println("Total Time: " + totalTime + " seconds");
        Player currentPlayer = getPlayer();
        String difficultyLabel = getDifficultyName(currentPlayer.getDifficultyLevel());
        if (difficultyLabel == null) {
            difficultyLabel = "Unknown";
        }
        outputService.println("Difficulty: " + difficultyLabel);
        outputService.println("Grid Size: " + getRows() + "x" + getCols());

        int previousTopScore = currentPlayer.getTopScore(getRows(), getCols());
        boolean newRecord = currentPlayer.updateTopScore(finalScore, getRows(), getCols());
        if (newRecord) {
            outputService.println("");
            outputService.println("🏆 NEW PERSONAL RECORD! 🏆");
            if (previousTopScore > 0) {
                outputService.println("Previous best: " + previousTopScore + " (improved by "
                        + (finalScore - previousTopScore) + ")");
            } else {
                outputService.println("This is your first completed game at this difficulty!");
            }
        }

        Map<Integer, Map<String, Integer>> allScores = currentPlayer.getAllTopScores();
        outputService.println("");
        outputService.println("=== YOUR TOP SCORES ===");
        if (allScores.isEmpty()) {
            outputService.println("No scores recorded yet.");
        } else {
            List<Integer> levels = new ArrayList<>(allScores.keySet());
            Collections.sort(levels);
            for (int level : levels) {
                Map<String, Integer> gridScores = allScores.get(level);
                if (gridScores == null || gridScores.isEmpty()) {
                    continue;
                }

                outputService.println(getDifficultyName(level) + " (Level " + level + "):");
                List<String> gridSizes = new ArrayList<>(gridScores.keySet());
                gridSizes.sort((a, b) -> {
                    int areaA = parseGridArea(a);
                    int areaB = parseGridArea(b);
                    if (areaA != areaB) {
                        return Integer.compare(areaA, areaB);
                    }
                    return a.compareTo(b);
                });

                for (String gridSize : gridSizes) {
                    outputService.println("  " + gridSize + " -> " + gridScores.get(gridSize));
                }
            }
        }
        outputService.println("");

        SlidingPuzzleLeaderboard.LeaderboardSnapshot leaderboardSnapshot = SlidingPuzzleLeaderboard.recordScore(
                currentPlayer.getName(), finalScore, getRows(), getCols(), difficultyLabel,
                currentPlayer.getDifficultyLevel());

        outputService.println("=== GLOBAL LEADERBOARD ===");
        List<SlidingPuzzleLeaderboard.LeaderboardEntry> globalTop = leaderboardSnapshot.getTopEntries();
        if (globalTop.isEmpty()) {
            outputService.println("No global scores recorded yet.");
        } else {
            int position = 1;
            for (SlidingPuzzleLeaderboard.LeaderboardEntry entry : globalTop) {
                outputService.println(String.format(Locale.ROOT,
                        "  %d. %s - %d pts (%s, %s) • %s",
                        position++, entry.getPlayerName(), entry.getScore(), entry.getDifficultyDisplay(),
                        entry.getGridLabel(), entry.getRecordedAtIso()));
            }
        }

        if (leaderboardSnapshot.getPlayerRank() > 0 && leaderboardSnapshot.getPlayerBest() != null) {
            SlidingPuzzleLeaderboard.LeaderboardEntry playerBest = leaderboardSnapshot.getPlayerBest();
            outputService.println(String.format(Locale.ROOT,
                    "Your best global rank: #%d with %d pts (%s, %s).",
                    leaderboardSnapshot.getPlayerRank(), playerBest.getScore(), playerBest.getDifficultyDisplay(),
                    playerBest.getGridLabel()));
        }

        outputService.println("=======================");
        outputService.println("");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Draws a text-based board with border art, tile labels, and current run
     * metrics such as moves and elapsed time.
     */
    @Override
    protected void displayGrid() {
        OutputService outputService = getOutputService();
        StringBuilder sb = new StringBuilder();

        sb.append(topLeftCorner);
        for (int j = 0; j < getCols(); j++) {
            sb.append(horizontalBorder);
        }
        sb.append("\n");

        for (int i = 0; i < getRows(); i++) {
            sb.append(verticalBorder);
            for (int j = 0; j < getCols(); j++) {
                Tile<SlidingPuzzlePiece> tile = gameGrid.getTile(i, j);
                SlidingPuzzlePiece piece = tile.getOccupant();
                if (piece == null || piece.isEmpty()) {
                    sb.append(emptyCell).append(verticalBorder);
                } else {
                    sb.append(String.format(cellFormat, piece.getDisplayToken())).append(verticalBorder);
                }

                if (tile.wasRecentlyUpdated()) {
                    tile.acknowledgeUpdate();
                }
            }

            sb.append("\n" + topLeftCorner);
            for (int j = 0; j < getCols(); j++) {
                sb.append(horizontalBorder);
            }
            sb.append("\n");
        }

        outputService.println(sb.toString());

        if (moveCount > 0) {
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            String currentDifficulty = getDifficultyName(getPlayer().getDifficultyLevel());
            outputService
                    .println("Moves: " + moveCount + " | Time: " + elapsedTime + "s | Current Score: " + currentScore
                            + " | Difficulty: " + currentDifficulty + " | Grid: " + getRows() + "x" + getCols());
            outputService.println(getPlayer().getName() + "'s Top Score (" + currentDifficulty + ", " + getRows() + "x"
                    + getCols() + "): " + getPlayer().getTopScore(getRows(), getCols()));
        }
    }

    /**
     * Parses a grid key (e.g., {@code 4x5}) into its total cell count.
     *
     * @param gridKey value in the format {@code rows x cols}
     * @return product of rows and columns, or {@link Integer#MAX_VALUE} when the
     *         key cannot be parsed
     */
    private int parseGridArea(String gridKey) {
        String[] parts = gridKey.split("x");
        if (parts.length != 2) {
            return Integer.MAX_VALUE;
        }
        try {
            int rows = Integer.parseInt(parts[0]);
            int cols = Integer.parseInt(parts[1]);
            return rows * cols;
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
    }
}
