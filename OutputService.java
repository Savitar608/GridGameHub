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
