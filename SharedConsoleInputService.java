/**
 * Variant of {@link ConsoleInputService} that shares a provided
 * {@link java.util.Scanner} instance and avoids closing the underlying stream
 * when {@link #close()} is invoked. Useful when multiple games should reuse the
 * same console input without terminating {@link System#in}.
 */
public final class SharedConsoleInputService extends ConsoleInputService {

    /**
     * Creates a shared input service around the supplied scanner.
     *
     * @param scanner shared scanner instance
     */
    public SharedConsoleInputService(java.util.Scanner scanner) {
        super(scanner);
    }

    @Override
    public void close() {
        // This is used in a shared context; do not close the scanner or else it will break in some place.
        return;
    }
}
