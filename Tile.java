
/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 *
 * File: Tile.java
 * Description: Represents a single cell on the puzzle grid, tracking its
 *              coordinates, the occupying {@link GamePiece}, and lightweight
 *              state metadata describing recent updates.
 */

import java.util.Objects;

/**
 * Represents a single cell on the puzzle grid, tracking its
 * coordinates, the occupying {@link GamePiece}, and lightweight
 * state metadata describing recent updates.
 *
 * @param <T> type of game piece that can occupy the tile
 */
public final class Tile<T extends GamePiece> {
    private final int row;
    private final int col;
    private T occupant;
    private boolean recentlyUpdated;
    private long lastUpdatedAt;

    /**
     * Creates a new tile at the specified coordinates.
     *
     * @param row      zero-based row index
     * @param col      zero-based column index
     * @param occupant initial game piece occupying the tile (may be {@code null})
     */
    public Tile(int row, int col, T occupant) {
        this.row = row;
        this.col = col;
        setOccupantInternal(occupant);
        this.recentlyUpdated = false;
    }

    /**
     * @return the row index associated with this tile
     */
    public int getRow() {
        return row;
    }

    /**
     * @return the column index associated with this tile
     */
    public int getCol() {
        return col;
    }

    /**
     * Retrieves the current game piece occupying the tile.
     *
     * @return the occupant piece, or {@code null} when empty
     */
    public T getOccupant() {
        return occupant;
    }

    /**
     * Replaces the current occupant with the provided piece.
     *
     * @param newOccupant the new piece occupying the tile
     */
    public void setOccupant(T newOccupant) {
        setOccupantInternal(Objects.requireNonNull(newOccupant, "newOccupant must not be null"));
        this.recentlyUpdated = true;
    }

    /**
     * Clears the tile so that it no longer holds a piece.
     */
    public void clear() {
        setOccupantInternal(null);
        this.recentlyUpdated = true;
    }

    /**
     * Indicates whether the tile currently contains a piece that should be
     * treated as empty space.
     *
     * @return {@code true} when the tile is empty
     */
    public boolean isEmpty() {
        return occupant == null || occupant.isEmpty();
    }

    /**
     * @return {@code true} if the tile was modified since the last time the flag
     *         was cleared
     */
    public boolean wasRecentlyUpdated() {
        return recentlyUpdated;
    }

    /**
     * Resets the "recently updated" flag once the change has been processed.
     */
    public void acknowledgeUpdate() {
        this.recentlyUpdated = false;
    }

    /**
     * @return system timestamp of the latest modification to the tile
     */
    public long getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    /**
     * Internal helper that updates the occupant reference and refreshes the last
     * modified timestamp without toggling the public update flag.
     *
     * @param newOccupant piece to store (may be {@code null} to represent an empty tile)
     */
    private void setOccupantInternal(T newOccupant) {
        this.occupant = newOccupant;
        this.lastUpdatedAt = System.currentTimeMillis();
    }
}
