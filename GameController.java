
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
import java.util.Scanner;

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

        Scanner scanner = game.getInputScanner();

        game.resetGameState();
        game.displayWelcomeMessage();
    configurePlayer(game);

        boolean keepPlaying = true;
        while (keepPlaying) {
            game.resetGameState();

            game.setSize();
            configureDifficulty(game);
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

            keepPlaying = promptPlayAgain(scanner);
        }

        System.out.println("Thanks for playing the Sliding Puzzle Game. Goodbye!");
        scanner.close();
    }

    private void configurePlayer(GridGame<?> game) {
        Player player = game.getPlayer();
        Scanner scanner = game.getInputScanner();
        player.promptForName(scanner);
    }

    private void configureDifficulty(GridGame<?> game) {
        DifficultyManager difficultyManager = game.getDifficultyManager();
        List<Integer> levels = difficultyManager.getSortedDifficultyLevels();
        if (levels.isEmpty()) {
            throw new IllegalStateException("No difficulty levels configured.");
        }

        Scanner scanner = game.getInputScanner();
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

        System.out.println("Hey " + player.getName() + ", choose your difficulty level: " + optionsBuilder);
        System.out.println("Note: The difficulty level increases exponentially with grid size");
        String chosenLevel = scanner.nextLine();

        int defaultLevel = difficultyManager.getMinDifficultyLevel();
        int level;
        try {
            level = Integer.parseInt(chosenLevel);
        } catch (Exception e) {
            System.out.println("Invalid input. Defaulting to " + difficultyManager.getDifficultyName(defaultLevel) + ".");
            level = defaultLevel;
        }

        player.setDifficultyLevel(level);

        if (!difficultyManager.isValidDifficultyLevel(level)) {
            System.out
                    .println("Invalid level. Defaulting to " + difficultyManager.getDifficultyName(defaultLevel) + ".");
            player.setDifficultyLevel(defaultLevel);
        }

        System.out.println("Difficulty set to: "
                + difficultyManager.getDifficultyName(player.getDifficultyLevel()));

        int currentTopScore = player.getTopScore(game.getRows(), game.getCols());
        if (currentTopScore > 0) {
            System.out.println("Your current top score for this difficulty: " + currentTopScore);
        } else {
            System.out.println("No previous score for this difficulty level.");
        }

        difficultyManager.displayDifficultyMessage(player.getDifficultyLevel());
    }

    /**
     * Prompts the user to decide whether to play again.
     *
     * @param scanner the scanner to read user input
     * @return {@code true} if the user wants to play again, {@code false} otherwise
     */
    private boolean promptPlayAgain(Scanner scanner) {
        while (true) {
            System.out.println("Would you like to play again? (yes/no)");
            String response;
            try {
                response = scanner.nextLine();
            } catch (Exception ex) {
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
                    System.out.println("Please respond with 'yes' or 'no'.");
            }
        }
    }
}
