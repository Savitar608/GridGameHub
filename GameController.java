
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
        game.setPlayerName();

        boolean keepPlaying = true;
        while (keepPlaying) {
            game.resetGameState();

            game.setSize();
            game.setDifficultyLevel();
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
