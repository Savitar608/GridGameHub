/**
 * File: DotsAndBoxes.java
 * Description: Application entry point that launches a Dots and Boxes match using the
 *              shared GridGame framework.
 */

/**
 * Entry point for launching a Dots and Boxes match using the shared
 * GridGame-based framework.
 */
public final class DotsAndBoxes {
    public static void main(String[] args) {
        DotsAndBoxesGame game = new DotsAndBoxesGame();
        game.startGame();
    }
}
