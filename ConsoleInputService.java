
/**
 * File: ConsoleInputService.java
 * Description: Console-backed implementation of {@link InputService} powered by
 *              {@link java.util.Scanner}. Provides line-based input retrieval
 *              with graceful handling of end-of-stream scenarios.
 */

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

/**
 * Reads user input from {@link System#in} by delegating to a {@link Scanner}.
 */
public class ConsoleInputService implements InputService {
    private final Scanner scanner;

    /**
     * Creates a console input service that wraps {@link System#in}.
     */
    public ConsoleInputService() {
        this(new Scanner(System.in));
    }

    /**
     * Creates an input service around the provided {@link Scanner}.
     *
     * @param scanner backing scanner instance (must not be {@code null})
     */
    public ConsoleInputService(Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "scanner must not be null");
    }

    /**
     * Reads the next line of user input from the underlying {@link Scanner}.
     *
     * @return the next line, or {@code null} if no further input is available
     */
    @Override
    public String readLine() {
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException | IllegalStateException ex) {
            return null;
        }
    }

    /**
     * Closes the underlying {@link Scanner} instance, releasing any associated
     * resources.
     */
    @Override
    public void close() {
        scanner.close();
    }
}
