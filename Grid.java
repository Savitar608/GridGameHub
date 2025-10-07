
/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 * 
 * File: Grid.java
 * Description: Generic grid class for managing 2D grid structures in grid-based games.
 *              Provides core functionality for grid operations, validation, and display.
 *              Supports any {@link GamePiece}-based tile through generics.
 * 
 * Features:
 * - 2D grid of {@link Tile} instances tracking occupants and metadata
 * - Grid validation and bounds checking
 * - Grid initialization and element access methods
 * - Grid size management and resizing capabilities
 * - Iterator support for grid traversal
 * - Display formatting support
 */

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Generic grid class for managing 2D grid structures comprised of {@link Tile}
 * objects that wrap {@link GamePiece} instances.
 *
 * @param <T> type of {@link GamePiece} stored within the grid tiles
 */
/**
 * Generic 2D grid container used by the games to manage tiles and pieces.
 * Provides utilities for safe access, resizing, and iteration.
 *
 * @param <T> type of GamePiece stored in the grid
 */
public class Grid<T extends GamePiece> {
    private Tile<T>[][] grid;
    private int rows;
    private int cols;
    private final Class<T> componentType;

    /**
     * Constructor to create a grid with specified dimensions and type.
     *
     * @param componentType The class type of elements to store in the grid
     * @param rows          The number of rows in the grid
     * @param cols          The number of columns in the grid
     * @throws IllegalArgumentException if rows or cols are less than 1
     */
    @SuppressWarnings("unchecked")
    public Grid(Class<T> componentType, int rows, int cols) {
        if (rows < 1 || cols < 1) {
            throw new IllegalArgumentException("Row and column sizes must be positive integers.");
        }

        this.componentType = componentType;
        this.rows = rows;
        this.cols = cols;

        this.grid = (Tile<T>[][]) new Tile[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Tile<>(i, j, null);
            }
        }
    }

    /**
     * Gets the number of rows in the grid.
     *
     * @return The number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the number of columns in the grid.
     *
     * @return The number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Gets the total number of cells in the grid.
     *
     * @return The total number of cells (rows * cols)
     */
    public int getSize() {
        return rows * cols;
    }

    /**
     * Gets the element at the specified position.
     *
     * @param row The row index (0-based)
     * @param col The column index (0-based)
     * @return The element at the specified position
     * @throws IndexOutOfBoundsException if the position is invalid
     */
    public Tile<T> getTile(int row, int col) {
        validateBounds(row, col);
        return grid[row][col];
    }

    /**
     * Gets the piece occupying the tile at the specified position.
     *
     * @param row The row index (0-based)
     * @param col The column index (0-based)
     * @return The game piece at the specified position, or {@code null} if empty
     * @throws IndexOutOfBoundsException if the position is invalid
     */
    public T getPiece(int row, int col) {
        return getTile(row, col).getOccupant();
    }

    /**
     * Sets the element at the specified position.
     *
     * @param row   The row index (0-based)
     * @param col   The column index (0-based)
     * @param value The value to set
     * @throws IndexOutOfBoundsException if the position is invalid
     */
    public void setPiece(int row, int col, T value) {
        validateBounds(row, col);
        grid[row][col].setOccupant(value);
    }

    /**
     * Clears the tile at the specified position.
     */
    public void clearTile(int row, int col) {
        validateBounds(row, col);
        grid[row][col].clear();
    }

    /**
     * Validates that the given row and column indices are within bounds.
     *
     * @param row The row index to validate
     * @param col The column index to validate
     * @throws IndexOutOfBoundsException if the indices are out of bounds
     */
    private void validateBounds(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException(
                    "Position (" + row + ", " + col + ") is out of bounds for grid of size " + rows + "x" + cols);
        }
    }

    /**
     * Checks if the given position is within the grid bounds.
     *
     * @param row The row index to check
     * @param col The column index to check
     * @return true if the position is valid, false otherwise
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    /**
     * Fills the entire grid with the specified value.
     *
     * @param value The value to fill the grid with
     */
    public void fill(T value) {
        if (value == null) {
            forEachTile(Tile::clear);
        } else {
            forEachTile(tile -> tile.setOccupant(value));
        }
    }

    /**
     * Fills the grid with values from the provided list in row-major order.
     *
     * @param values The list of values to fill the grid with
     * @throws IllegalArgumentException if the list size doesn't match grid size
     */
    public void fillFromList(List<T> values) {
        if (values.size() != rows * cols) {
            throw new IllegalArgumentException(
                    "List size (" + values.size() + ") must match grid size (" + (rows * cols) + ")");
        }

        // Fill using iterator to preserve insertion order provided by caller
        Iterator<T> iterator = values.iterator();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j].setOccupant(iterator.next());
            }
        }
    }

    /**
     * Finds the position of the first occurrence of the specified value.
     *
     * @param value The value to search for
     * @return An array containing [row, col] of the first occurrence, or null if
     *         not found
     */
    public int[] findPosition(T value) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Objects.equals(grid[i][j].getOccupant(), value)) {
                    return new int[] { i, j };
                }
            }
        }
        return null;
    }

    /**
     * Swaps the elements at two positions in the grid.
     *
     * @param row1 The row of the first position
     * @param col1 The column of the first position
     * @param row2 The row of the second position
     * @param col2 The column of the second position
     * @throws IndexOutOfBoundsException if any position is invalid
     */
    public void swap(int row1, int col1, int row2, int col2) {
        validateBounds(row1, col1);
        validateBounds(row2, col2);

        T temp = grid[row1][col1].getOccupant();
        grid[row1][col1].setOccupant(grid[row2][col2].getOccupant());
        grid[row2][col2].setOccupant(temp);
    }

    /**
     * Creates a copy of the current grid.
     *
     * @return A new Grid instance with the same contents
     */
    public Grid<T> copy() {
        Grid<T> newGrid = new Grid<>(componentType, rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                T occupant = grid[i][j].getOccupant();
                if (occupant != null) {
                    newGrid.setPiece(i, j, occupant);
                }
            }
        }
        return newGrid;
    }

    /**
     * Resizes the grid to new dimensions.
     * Content is preserved where possible, new cells are set to null.
     *
     * @param newRows The new number of rows
     * @param newCols The new number of columns
     * @throws IllegalArgumentException if new dimensions are invalid
     */
    @SuppressWarnings("unchecked")
    public void resize(int newRows, int newCols) {
        if (newRows < 1 || newCols < 1) {
            throw new IllegalArgumentException("Row and column sizes must be positive integers.");
        }

        // Create new grid
        Tile<T>[][] newGrid = (Tile<T>[][]) new Tile[newRows][newCols];
        for (int i = 0; i < newRows; i++) {
            for (int j = 0; j < newCols; j++) {
                newGrid[i][j] = new Tile<>(i, j, null);
            }
        }

        // Copy overlapping region from the old grid into the new grid
        int copyRows = Math.min(rows, newRows);
        int copyCols = Math.min(cols, newCols);
        for (int i = 0; i < copyRows; i++) {
            for (int j = 0; j < copyCols; j++) {
                T occupant = grid[i][j].getOccupant();
                if (occupant != null) {
                    newGrid[i][j].setOccupant(occupant);
                }
            }
        }

        this.grid = newGrid;
        this.rows = newRows;
        this.cols = newCols;
    }

    /**
     * Returns a string representation of the grid for debugging.
     *
     * @return String representation of the grid
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Grid[").append(rows).append("x").append(cols).append("]:\n");
        for (int i = 0; i < rows; i++) {
            sb.append("[");
            for (int j = 0; j < cols; j++) {
                sb.append(grid[i][j].getOccupant());
                if (j < cols - 1)
                    sb.append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    /**
     * Provides access to the raw grid array for advanced operations.
     * WARNING: Direct modification of the returned array can break grid invariants.
     *
     * @return The raw 2D array (use with caution)
     */
    public Tile<T>[][] getRawGrid() {
        return grid;
    }

    /**
     * Executes the provided action for every tile within the grid.
     *
     * @param action consumer invoked once per tile in row-major order
     */
    private void forEachTile(java.util.function.Consumer<Tile<T>> action) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                action.accept(grid[i][j]);
            }
        }
    }
}