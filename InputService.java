/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 *
 * File: InputService.java
 * Description: Interface abstraction for user input, enabling alternate input
 *              providers to be swapped in for testing or platform-specific needs.
 */

/**
 * Abstraction for reading textual input from the user. Implementations may
 * read from the console, from test fixtures, or from other sources.
 */
public interface InputService extends AutoCloseable {
    /**
     * Reads the next available line of input.
     *
     * @return the next input line, or {@code null} when end-of-stream is reached
     */
    String readLine();

    /**
     * Close and release any underlying resources associated with the input
     * source (for example, a Scanner or stream).
     */
    @Override
    void close();
}
