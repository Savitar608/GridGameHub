/**
 * Simple launcher for the Dots & Boxes game.
 *
 * This class provides a tiny entry point that constructs a
 * {@link DotsAndBoxesGame} with colored edges enabled and starts the
 * interactive game loop. It is intended to be used from the command line
 * or for quick manual testing.
 */
public final class DotsAndBoxes {
    /**
     * Starts the Dots & Boxes interactive session. The arguments are ignored.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        DotsAndBoxesGame game = new DotsAndBoxesGame(true);
        game.startGame();
    }
}
