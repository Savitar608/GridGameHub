/**
 * Utility launcher used during development to quickly render a sample
 * Dots & Boxes board to stdout. This class exists solely to produce a
 * readable board dump and is not part of the interactive game flow.
 */
public class BoardDump {
    /**
     * Entry point used by the developer to instantiate a small
     * DotsAndBoxesGame and print its initial board state.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        // Create a DotsAndBoxesGame with colored edges off to get a simple render
        DotsAndBoxesGame game = new DotsAndBoxesGame(false);

        // Use defaults (3x3 boxes => grid 5x5). initializeGame() is safe to call
        // non-interactively because Player has a default name and setSize() uses defaults.
        game.initializeGame();
        game.displayGrid();
    }
}
