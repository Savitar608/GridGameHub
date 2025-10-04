/**
 * Console-backed implementation of {@link InputService} using {@link java.util.Scanner}.
 */
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

public class ConsoleInputService implements InputService {
    private final Scanner scanner;

    public ConsoleInputService() {
        this(new Scanner(System.in));
    }

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
