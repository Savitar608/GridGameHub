/**
 * File: GamePiece.java
 * Description: Core abstraction for board game pieces, enabling concrete games
 *              to supply custom metadata and rendering for their individual pieces.
 */

/**
 * Core abstraction for board game pieces, enabling concrete games
 * to supply custom metadata and rendering for their individual pieces.
 */
public interface GamePiece {
    /**
     * Human-readable token used when rendering the piece on the board.
     *
     * @return non-null display string (may be empty for invisible pieces)
     */
    String getDisplayToken();

    /**
     * Indicates whether this piece represents an empty space.
     *
     * @return {@code true} when the piece should be treated as empty
     */
    boolean isEmpty();
}
