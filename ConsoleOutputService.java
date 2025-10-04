/**
 * Console-backed implementation of {@link OutputService} that delegates to
 * {@link java.io.PrintStream}.
 */
import java.io.PrintStream;
import java.util.Objects;

public class ConsoleOutputService implements OutputService {
    private final PrintStream printStream;

    public ConsoleOutputService() {
        this(System.out);
    }

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
