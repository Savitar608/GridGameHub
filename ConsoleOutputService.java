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

    @Override
    public void print(String message) {
        printStream.print(message);
    }

    @Override
    public void println(String message) {
        printStream.println(message);
    }

    @Override
    public void flush() {
        printStream.flush();
    }
}
