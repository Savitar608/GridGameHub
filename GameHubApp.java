import java.util.Locale;

/**
 * Main application that displays the top-level menu and launches games.
 * Responsibilities include wiring input/output services and the scoreboard.
 */
public final class GameHubApp {
    // I/O + scoreboard
    private final InputService in = new ConsoleInputService();
    private final OutputService out = new ConsoleOutputService();
    private final Scoreboard scoreboard = new Scoreboard();

    /**
     * Main menu loop presenting available games and dispatching to launch methods.
     */
    public void start() {
        while (true) {
            out.println("");
            out.println("============== GAME HUB ==============");
            out.println("1) Sliding Puzzle");
            out.println("2) Dots & Boxes");
            out.println("3) Quit");
            out.print("Choose an option (1-3): ");

            String raw = in.readLine();
            if (raw == null) { // EOF or input stream closed
                out.println("\nInput closed. Goodbye!");
                return;
            }

            // normalize to lower-case for easier matching
            String choice = raw.trim().toLowerCase(Locale.ROOT);
            if (choice.isEmpty()) {
                out.println("Invalid option. Try 1, 2, 3 or q.");
                continue;
            }

            switch (choice) {
                case "1":
                    launchSlidingPuzzle();
                    break;
                case "2":
                    launchDotsAndBoxes();
                    break;
                case "3":
                case "q":
                case "quit":
                case "exit":
                    out.println("Goodbye!");
                    return; // <-- exit cleanly
                default:
                    out.println("Invalid option. Try 1, 2, 3 or q.");
            }
        }
    }

    /** Launches the Sliding Puzzle game and prints a summary on return. */
    private void launchSlidingPuzzle() {
        out.println("\n--- Launching Sliding Puzzle ---\n");
        try {
            SlidingPuzzleGame game = new SlidingPuzzleGame(in, out);
            game.startGame();
        } catch (Throwable t) {
            out.println("Couldn't start Sliding Puzzle. Did you compile all files?");
            t.printStackTrace(System.out);
        } finally {
            out.println("\n[Scoreboard]\n" + scoreboard);
            out.println("\n(Returning to Game Hub...)");
        }
    }

    /** Launches the Dots & Boxes game and records the result on the scoreboard. */
    private void launchDotsAndBoxes() {
        boolean colored = true; // default ON
        out.println("\n--- Launching Dots & Boxes --- (colored edges ON)\n");
        DotsAndBoxesGame game = new DotsAndBoxesGame(colored, in, out);
        try {
            game.startGame();
        } catch (Throwable t) {
            out.println("Couldn't start Dots & Boxes. Did you compile all files?");
            t.printStackTrace(System.out);
            return;
        }

        String p1 = game.getPlayer1Name();
        String p2 = game.getPlayer2Name();
        int s1 = game.getP1Score();
        int s2 = game.getP2Score();
        String winner = game.getWinnerName();

        if (winner != null && !winner.equalsIgnoreCase("draw")) {
            scoreboard.recordWin(winner);
        } else {
            scoreboard.recordDraw();
        }

        out.println("\n===== Match Summary (Dots & Boxes) =====");
        out.println(p1 + ": " + s1 + " | avg move: " + String.format("%.2f", game.getP1AvgSeconds()) + "s");
        out.println(p2 + ": " + s2 + " | avg move: " + String.format("%.2f", game.getP2AvgSeconds()) + "s");
        out.println(winner == null || winner.equalsIgnoreCase("draw") ? "Result: Draw" : ("Winner: " + winner));
        out.println("\n[Scoreboard]\n" + scoreboard);
        out.println("\n(Returning to Game Hub...)");
    }
}
