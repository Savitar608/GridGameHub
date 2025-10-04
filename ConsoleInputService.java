/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 *
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

    @Override
    public String readLine() {
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException | IllegalStateException ex) {
            return null;
        }
    }

    @Override
    public void close() {
        scanner.close();
    }
}
