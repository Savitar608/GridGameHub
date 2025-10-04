/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 *
 * File: OutputService.java
 * Description: Interface abstraction for output operations, allowing the
 *              console renderer to be replaced during testing or when targeting
 *              alternative environments.
 */

/**
 * Abstraction over output operations to simplify testing of console interactions.
 */
public interface OutputService {
    /**
     * Prints text without a trailing newline.
     *
     * @param message text to print
     */
    void print(String message);

    /**
     * Prints text followed by a newline.
     *
     * @param message text to print
     */
    void println(String message);

    /**
     * Flushes any buffered output.
     */
    void flush();
}
