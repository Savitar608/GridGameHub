
/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 * 
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
 * 
 * @author
 * @version 2.0
 * @date October 4, 2025
 * @course CS611 - Object Oriented Design
 * @assignment Assignment 1
 */

import java.util.List;
import java.util.Locale;
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
        configurePlayer(game, inputService, outputService);

        boolean keepPlaying = true;
        while (keepPlaying) {
            game.resetGameState();

            game.setSize();
            configureDifficulty(game, inputService, outputService);
            game.initializeGame();

            while (!game.isGameOver()) {
                game.displayGrid();
                game.processUserInput();

                if (game.isGameOver()) {
                    break;
                }

                if (game.checkWinCondition()) {
                    game.setGameOver(true);
                }
            }

            game.displayGrid();
            game.displayWinMessage();

            keepPlaying = promptPlayAgain(inputService, outputService);
        }

        outputService.println("Thanks for playing the Sliding Puzzle Game. Goodbye!");
        inputService.close();
    }

    private void configurePlayer(GridGame<?> game, InputService inputService, OutputService outputService) {
        Player player = game.getPlayer();
        player.promptForName(inputService, outputService);
    }

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
        String chosenLevel = inputService.readLine();

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
    private boolean promptPlayAgain(InputService inputService, OutputService outputService) {
        while (true) {
            outputService.println("Would you like to play again? (yes/no)");
            String response = inputService.readLine();
            if (response == null) {
                return false;
            }

            response = response.trim().toLowerCase(Locale.ROOT);
            switch (response) {
                case "yes":
                case "y":
                    return true;
                case "no":
                case "n":
                    return false;
                default:
                    outputService.println("Please respond with 'yes' or 'no'.");
            }
        }
    }
}
