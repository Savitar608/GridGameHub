/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 *
 * File: GamePiece.java
 * Description: Core abstraction for board game pieces, enabling concrete games
 *              to supply custom metadata and rendering for their individual pieces.
 */

/**
 * Minimal interface for an object that can be placed on a game grid. Each
 * implementation provides a short token for display and a flag indicating if
 * the piece should be treated as empty for game logic.
 */
public interface GamePiece {
    /**
     * Text token used when rendering the piece.
     *
     * @return display token (not null)
     */
    String getDisplayToken();

    /**
     * Returns true when this piece should be considered empty by game logic.
     *
     * @return {@code true} if empty
     */
    boolean isEmpty();
}
