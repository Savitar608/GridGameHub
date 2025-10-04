/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 *
 * File: ConsoleOutputService.java
 * Description: Console-backed implementation of {@link OutputService} that
 *              delegates all writes to a {@link java.io.PrintStream}.
 */

import java.io.PrintStream;
import java.util.Objects;

/**
 * Writes output to {@link System#out} (or a provided {@link PrintStream}).
 */
public class ConsoleOutputService implements OutputService {
    private final PrintStream printStream;

    /**
     * Constructs a console output service backed by {@link System#out}.
     */
    public ConsoleOutputService() {
        this(System.out);
    }

    /**
     * Constructs an output service that writes to the specified {@link PrintStream}.
     *
     * @param printStream destination stream (must not be {@code null})
     */
    public ConsoleOutputService(PrintStream printStream) {
        this.printStream = Objects.requireNonNull(printStream, "printStream must not be null");
    }

    /**
     * Writes the provided message to the configured {@link PrintStream} without
     * appending a newline.
     *
     * @param message text to emit
     */
    @Override
    public void print(String message) {
        printStream.print(message);
    }

    /**
     * Writes the provided message followed by the platform newline to the
     * configured {@link PrintStream}.
     *
     * @param message line of text to emit
     */
    @Override
    public void println(String message) {
        printStream.println(message);
    }

    /**
     * Flushes any buffered output on the underlying {@link PrintStream} to ensure
     * it appears immediately.
     */
    @Override
    public void flush() {
        printStream.flush();
    }
}
