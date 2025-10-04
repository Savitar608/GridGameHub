/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 * 
 * File: Grid.java
 * Description: Generic grid class for managing 2D grid structures in grid-based games.
 *              Provides core functionality for grid operations, validation, and display.
 *              Supports any data type through generics.
 * 
 * Features:
 * - Generic 2D grid structure supporting any data type
 * - Grid validation and bounds checking
 * - Grid initialization and element access methods
 * - Grid size management and resizing capabilities
 * - Iterator support for grid traversal
 * - Display formatting support
 * 
 * @author Adithya Lnu
 * @version 2.0
 * @date October 4, 2025
 * @course CS611 - Object Oriented Design
 * @assignment Assignment 1
 */

import java.lang.reflect.Array;
import java.util.*;

/**
 * Generic grid class for managing 2D grid structures.
 * Supports any data type through generics and provides essential grid operations.
 *
 * @param <T> The type of elements stored in the grid
 */
public class Grid<T> {
    private T[][] grid;
    private int rows;
    private int cols;
    private Class<T> componentType;

    /**
     * Constructor to create a grid with specified dimensions and type.
     *
     * @param componentType The class type of elements to store in the grid
     * @param rows The number of rows in the grid
     * @param cols The number of columns in the grid
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
        
        // Create a 2D generic array properly using reflection
        T[][] tempGrid = (T[][]) Array.newInstance(componentType, rows, 0);
        for (int i = 0; i < rows; i++) {
            tempGrid[i] = (T[]) Array.newInstance(componentType, cols);
        }
        this.grid = tempGrid;
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
    public T get(int row, int col) {
        validateBounds(row, col);
        return grid[row][col];
    }

    /**
     * Sets the element at the specified position.
     *
     * @param row The row index (0-based)
     * @param col The column index (0-based)
     * @param value The value to set
     * @throws IndexOutOfBoundsException if the position is invalid
     */
    public void set(int row, int col, T value) {
        validateBounds(row, col);
        grid[row][col] = value;
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
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = value;
            }
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
        
        Iterator<T> iterator = values.iterator();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = iterator.next();
            }
        }
    }

    /**
     * Finds the position of the first occurrence of the specified value.
     *
     * @param value The value to search for
     * @return An array containing [row, col] of the first occurrence, or null if not found
     */
    public int[] findPosition(T value) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Objects.equals(grid[i][j], value)) {
                    return new int[]{i, j};
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
        
        T temp = grid[row1][col1];
        grid[row1][col1] = grid[row2][col2];
        grid[row2][col2] = temp;
    }

    /**
     * Creates a copy of the current grid.
     *
     * @return A new Grid instance with the same contents
     */
    @SuppressWarnings("unchecked")
    public Grid<T> copy() {
        Grid<T> newGrid = new Grid<>(componentType, rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                newGrid.set(i, j, this.get(i, j));
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
        T[][] newGrid = (T[][]) Array.newInstance(componentType, newRows, 0);
        for (int i = 0; i < newRows; i++) {
            newGrid[i] = (T[]) Array.newInstance(componentType, newCols);
        }
        
        // Copy existing content
        int copyRows = Math.min(rows, newRows);
        int copyCols = Math.min(cols, newCols);
        for (int i = 0; i < copyRows; i++) {
            for (int j = 0; j < copyCols; j++) {
                newGrid[i][j] = grid[i][j];
            }
        }
        
        // Update grid properties
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
                sb.append(grid[i][j]);
                if (j < cols - 1) sb.append(", ");
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
    public T[][] getRawGrid() {
        return grid;
    }
}