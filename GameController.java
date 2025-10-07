
/**
 * File: GameController.java
 * Description: Controller class responsible for orchestrating the flow of a grid-based game.
 *              Handles the high-level game loop, replay prompts, and coordinates
 *              between the user interface provided by the game implementation and
 *              player interactions.
 * 
 * Features:
 * - Centralized game loop management separated from game logic
 * - Replay handling with safe input processing
 * - Extensible design supporting any {@link GridGame} implementation
 * - Clean resource management for input services
 * - User-friendly prompts and feedback during gameplay
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Coordinates the execution of a {@link GridGame} by running the game loop and
 * managing replay prompts.
 */
public class GameController {

    /**
     * Starts the interactive game session and manages replay requests.
     *
     * @param game the game to control
     */
    public void run(GridGame<?> game) {
        Objects.requireNonNull(game, "game must not be null");

        InputService inputService = game.getInputService();
        OutputService outputService = game.getOutputService();

        game.resetGameState();
        game.displayWelcomeMessage();
        boolean continueGame = game.configurePlayers(inputService, outputService);

        if (!continueGame || game.isExitRequested()) {
            outputService.println("Exiting game. Goodbye!");
            inputService.close();
            return;
        }

        boolean keepPlaying = true;
        while (keepPlaying && !game.isExitRequested()) {
            game.resetGameState();

            game.setSize();
            if (game.isExitRequested()) {
                break;
            }

            if (game.supportsDifficultySelection()) {
                configureDifficulty(game, inputService, outputService);
                if (game.isExitRequested()) {
                    break;
                }
            }

            game.initializeGame();
            if (game.isExitRequested()) {
                break;
            }

            game.displayGrid();
            if (!presentPreGameOptions(game, inputService, outputService)) {
                break;
            }

            while (!game.isExitRequested() && !game.isGameOver()) {
                game.displayGrid();
                game.processUserInput();

                if (game.isExitRequested()) {
                    break;
                }

                if (game.isGameOver()) {
                    break;
                }

                if (game.checkWinCondition()) {
                    game.setGameOver(true);
                }
            }

            if (game.isExitRequested()) {
                break;
            }

            game.displayGrid();
            game.displayWinMessage();

            if (game.isExitRequested()) {
                break;
            }

            keepPlaying = promptPlayAgain(game, inputService, outputService);
        }

    outputService.println("Thanks for playing! Goodbye!");
        inputService.close();
    }

    /**
     * Prompts the player to choose a difficulty level and applies any necessary
     * validation or default fallbacks.
     *
     * @param game          the game providing difficulty options
     * @param inputService  input source for player selection
     * @param outputService destination for informational messages
     */
    private void configureDifficulty(GridGame<?> game, InputService inputService, OutputService outputService) {
        DifficultyManager difficultyManager = game.getDifficultyManager();
        List<Integer> levels = difficultyManager.getSortedDifficultyLevels();
        if (levels.isEmpty()) {
            throw new IllegalStateException("No difficulty levels configured.");
        }

        Player player = game.getPlayer();

        StringBuilder optionsBuilder = new StringBuilder();
        for (int i = 0; i < levels.size(); i++) {
            int level = levels.get(i);
            optionsBuilder.append(level).append(" (")
                    .append(difficultyManager.getDifficultyName(level))
                    .append(")");
            if (i < levels.size() - 1) {
                optionsBuilder.append(", ");
            }
        }

        outputService.println("Hey " + player.getName() + ", choose your difficulty level: " + optionsBuilder);
        outputService.println("Note: The difficulty level increases exponentially with grid size");
        outputService.println("Type 'quit' at any prompt to exit the game.");
        String chosenLevel = inputService.readLine();

        if (chosenLevel == null || game.isQuitCommand(chosenLevel)) {
            game.requestExit();
            return;
        }

        chosenLevel = chosenLevel.trim();

        int defaultLevel = difficultyManager.getMinDifficultyLevel();
        int level;
        try {
            level = Integer.parseInt(chosenLevel);
        } catch (Exception e) {
            outputService.println(
                    "Invalid input. Defaulting to " + difficultyManager.getDifficultyName(defaultLevel) + ".");
            level = defaultLevel;
        }

        player.setDifficultyLevel(level);

        if (!difficultyManager.isValidDifficultyLevel(level)) {
            outputService.println(
                    "Invalid level. Defaulting to " + difficultyManager.getDifficultyName(defaultLevel) + ".");
            player.setDifficultyLevel(defaultLevel);
        }

        outputService.println("Difficulty set to: "
                + difficultyManager.getDifficultyName(player.getDifficultyLevel()));

        int currentTopScore = player.getTopScore(game.getRows(), game.getCols());
        if (currentTopScore > 0) {
            outputService.println("Your current top score for this difficulty: " + currentTopScore);
        } else {
            outputService.println("No previous score for this difficulty level.");
        }

        difficultyManager.displayDifficultyMessage(player.getDifficultyLevel(), outputService);
    }

    /**
     * Prompts the user to decide whether to play again.
     *
     * @param inputService  source of user responses
     * @param outputService destination for prompt messages
     * @return {@code true} if the user wants to play again, {@code false} otherwise
     */
    private boolean promptPlayAgain(GridGame<?> game, InputService inputService, OutputService outputService) {
        while (true) {
            outputService.println("Would you like to play again? (yes/no/quit)");
            String response = inputService.readLine();
            if (response == null) {
                return false;
            }

            response = response.trim().toLowerCase(Locale.ROOT);
            if (game.isQuitCommand(response)) {
                game.requestExit();
                return false;
            }

            switch (response) {
                case "yes":
                case "y":
                    return true;
                case "no":
                case "n":
                    return false;
                default:
                    outputService.println("Please respond with 'yes', 'no', or 'quit'.");
            }
        }
    }

    /**
     * Presents the player with pre-game actions such as regenerating the board or
     * viewing scores before starting the run.
     *
     * @param game          active game instance orchestrating state
     * @param inputService  player input source
     * @param outputService destination for prompts and feedback
     * @return {@code true} when gameplay should begin; {@code false} if the
     *         player exits the pre-game menu
     */
    private boolean presentPreGameOptions(GridGame<?> game, InputService inputService, OutputService outputService) {
        while (!game.isExitRequested()) {
            boolean regenSupported = game.supportsBoardRegeneration();
            StringBuilder menu = new StringBuilder("Choose an option: [start] Begin game");
            if (regenSupported) {
                menu.append(", [regen] Regenerate board");
            }
            if (game.supportsTopScores()) {
                menu.append(", [scores] View top scores");
            }
            menu.append(", [quit] Exit");
            outputService.println(menu.toString());
            String choice = inputService.readLine();

            if (choice == null || game.isQuitCommand(choice)) {
                game.requestExit();
                return false;
            }

            String normalized = choice.trim().toLowerCase(Locale.ROOT);
            switch (normalized) {
                case "start":
                case "s":
                    return true;
                case "regen":
                case "r":
                case "regenerate":
                    if (regenSupported) {
                        game.initializeGame();
                        if (game.isExitRequested()) {
                            return false;
                        }
                        outputService.println("Board regenerated.");
                        game.displayGrid();
                    } else {
                        outputService.println("Board regeneration is not available for this game.");
                    }
                    break;
                case "scores":
                case "score":
                case "top":
                    if (game.supportsTopScores()) {
                        displayPlayerTopScores(game, outputService);
                    } else {
                        outputService.println("Top scores are not tracked for this game mode.");
                    }
                    break;
                default:
                    outputService.println("Please choose 'start', 'regen', 'scores', or 'quit'.");
            }
        }
        return false;
    }

    /**
     * Renders the player's recorded top scores grouped by difficulty level and
     * grid size.
     *
     * @param game          active game providing context and difficulty labels
     * @param outputService destination for score output
     */
    private void displayPlayerTopScores(GridGame<?> game, OutputService outputService) {
        List<Player> players = game.getPlayers();
        outputService.println("");
        outputService.println("=== TOP SCORES ===");

        for (Player player : players) {
            Map<Integer, Map<String, Integer>> allScores = player.getAllTopScores();
            outputService.println(player.getName() + ":");
            if (allScores.isEmpty()) {
                outputService.println("  No scores recorded yet.");
                continue;
            }

            List<Integer> levels = new ArrayList<>(allScores.keySet());
            Collections.sort(levels);
            for (int level : levels) {
                Map<String, Integer> gridScores = allScores.get(level);
                if (gridScores == null || gridScores.isEmpty()) {
                    continue;
                }

                String difficultyName = game.getDifficultyManager().getDifficultyName(level);
                outputService.println("  " + difficultyName + " (Level " + level + "):" );

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
                    outputService.println("    " + gridSize + " -> " + gridScores.get(gridSize));
                }
            }
        }

        outputService.println("=======================");
        outputService.println("");
    }

    /**
     * Converts a grid size key into a comparable area value for sorting.
     *
     * @param gridKey textual representation in the form {@code rows x cols}
     * @return computed area, or {@link Integer#MAX_VALUE} when parsing fails
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
