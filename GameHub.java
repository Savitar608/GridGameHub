/**
 * File: GameHub.java
 * Description: Central launcher providing a menu for choosing between the console
 *              games built on the shared grid framework.
 */

/**
 * Central launcher presenting a simple menu for selecting between the available
 * games built on the grid framework.
 */
public final class GameHub {
    private GameHub() {
        // Utility class; prevent instantiation.
    }

    /**
     * Entry point presenting the hub menu, routing users to the available games,
     * or exiting when requested.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        java.util.Scanner sharedScanner = new java.util.Scanner(System.in);
        SharedConsoleInputService sharedInput = new SharedConsoleInputService(sharedScanner);
        ConsoleOutputService outputService = new ConsoleOutputService();

        boolean running = true;
        while (running) {
            outputService.println("===================================");
            outputService.println("           GAME HUB MENU            ");
            outputService.println("===================================");
            outputService.println("1. Sliding Puzzle");
            outputService.println("2. Dots and Boxes");
            outputService.println("3. Quit");
            outputService.print("Select an option (1-3): ");
            outputService.flush();

            String selection = sharedInput.readLine();
            if (selection == null) {
                outputService.println("\nNo input detected. Exiting Game Hub.");
                break;
            }

            String trimmed = selection.trim().toLowerCase(java.util.Locale.ROOT);
            switch (trimmed) {
                case "1":
                case "sliding":
                case "sliding puzzle":
                    launchSlidingPuzzle(sharedInput, outputService);
                    break;
                case "2":
                case "dots":
                case "dots and boxes":
                    launchDotsAndBoxes(sharedInput, outputService);
                    break;
                case "3":
                case "q":
                case "quit":
                case "exit":
                    running = false;
                    break;
                default:
                    outputService.println("Invalid selection. Please choose 1, 2, or 3.\n");
            }
        }

        outputService.println("Goodbye!");
        sharedScanner.close();
    }

    /**
     * Launches the sliding puzzle game using the shared I/O services.
     *
     * @param sharedInput   shared input service instance
     * @param outputService output destination for hub/game messaging
     */
    private static void launchSlidingPuzzle(InputService sharedInput, OutputService outputService) {
        outputService.println("\nLoading Sliding Puzzle...\n");
        GridGame<?> game = new SlidingPuzzleGame(sharedInput, outputService);
        game.startGame();
        outputService.println("\nReturning to Game Hub...\n");
    }

    /**
     * Launches the Dots and Boxes game using the shared I/O services.
     *
     * @param sharedInput   shared input service instance
     * @param outputService output destination for hub/game messaging
     */
    private static void launchDotsAndBoxes(InputService sharedInput, OutputService outputService) {
        outputService.println("\nLoading Dots and Boxes...\n");
        GridGame<?> game = new DotsAndBoxesGame(sharedInput, outputService);
        game.startGame();
        outputService.println("\nReturning to Game Hub...\n");
    }
}
