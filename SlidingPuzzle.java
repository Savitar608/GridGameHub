/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 * 
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
 * 
 * @author Adithya Lnu
 * @version 2.0
 * @date October 4, 2025
 * @course CS611 - Object Oriented Design
 * @assignment Assignment 1
 */

public final class SlidingPuzzle {
    // Main method to start the game
    public static void main(String[] args) {
        SlidingPuzzleGame game = new SlidingPuzzleGame();
        game.startGame();
    }
}