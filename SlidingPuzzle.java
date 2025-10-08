/**
 * File: SlidingPuzzle.java
 * Description: A complete sliding puzzle game implementation featuring an extensible
 *              grid-based game framework with support for multiple difficulty levels,
 *              score tracking, and player management.
 * 
 * Features:
 * - Extensible GridGame abstract base class for terminal-based grid games
 * - SlidingPuzzleGame implementation with configurable grid sizes (3x3 to 20x20)
 * - Multiple difficulty levels (Easy, Medium, Hard, Expert, Master, Legendary)
 * - Score calculation based on moves, time, difficulty, and grid size
 * - Top score tracking per difficulty level
 * - Player management with separate Player class
 * - Solvability guarantee through controlled shuffling
 */

/**
 * Entry point for launching the sliding puzzle application.
 */
public final class SlidingPuzzle {
    /**
     * Boots the console-based sliding puzzle game using the default
     * input/output services.
     *
     * @param args ignored command-line parameters
     */
    public static void main(String[] args) {
        SlidingPuzzleGame game = new SlidingPuzzleGame();
        game.startGame();
    }
}