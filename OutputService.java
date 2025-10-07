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
 * Abstraction for writing textual output so the game can be tested or run
 * with different output sinks (console, tests, GUIs).
 */
public interface OutputService {
    /**
     * Print text with no trailing newline.
     *
     * @param message message to print
     */
    void print(String message);

    /**
     * Print text followed by a newline.
     *
     * @param message message to print
     */
    void println(String message);

    /**
     * Flush any buffered output so it appears immediately.
     */
    void flush();
}
