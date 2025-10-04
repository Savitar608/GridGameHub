/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 *
 * File: SlidingPuzzlePiece.java
 * Description: Concrete {@link GamePiece} representation for numbered tiles in
 * the sliding puzzle. Encapsulates the tile's numeric value and
 * empty-slot semantics, enabling future expansion with additional
 * piece metadata.
 */

/**
 * Concrete {@link GamePiece} representation for numbered tiles in
 * the sliding puzzle. Encapsulates the tile's numeric value and
 * empty-slot semantics, enabling future expansion with additional
 * piece metadata.
 */
public final class SlidingPuzzlePiece implements GamePiece {
    private static final int EMPTY_VALUE = 0;
    private static final SlidingPuzzlePiece EMPTY_PIECE = new SlidingPuzzlePiece(EMPTY_VALUE);

    private final int value;

    private SlidingPuzzlePiece(int value) {
        this.value = value;
    }

    /**
     * Factory method for creating a numbered puzzle piece. The value {@code 0}
     * is reserved for the empty slot and will return the shared empty instance.
     *
     * @param value numeric identifier of the piece
     * @return puzzle piece representing the provided value
     */
    public static SlidingPuzzlePiece ofValue(int value) {
        if (value == EMPTY_VALUE) {
            return EMPTY_PIECE;
        }
        return new SlidingPuzzlePiece(value);
    }

    /**
     * Provides the shared instance representing the empty slot.
     *
     * @return empty puzzle piece
     */
    public static SlidingPuzzlePiece empty() {
        return EMPTY_PIECE;
    }

    /**
     * Returns the numeric value associated with this piece.
     *
     * @return numeric tile value (0 represents the empty slot)
     */
    public int getValue() {
        return value;
    }

    /**
     * Convenience predicate for comparing the piece's numeric value.
     *
     * @param candidate value to compare against
     * @return {@code true} when the values match
     */
    public boolean hasValue(int candidate) {
        return value == candidate;
    }

    @Override
    public String getDisplayToken() {
        return isEmpty() ? "" : Integer.toString(value);
    }

    @Override
    public boolean isEmpty() {
        return value == EMPTY_VALUE;
    }

    @Override
    public String toString() {
        return isEmpty() ? "SlidingPuzzlePiece{empty}" : "SlidingPuzzlePiece{" + value + "}";
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SlidingPuzzlePiece other = (SlidingPuzzlePiece) obj;
        return value == other.value;
    }
}
