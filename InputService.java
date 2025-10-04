/**
 * Defines an abstraction for reading user input, enabling alternate input
 * sources for testing.
 */
public interface InputService extends AutoCloseable {
    /**
     * Reads the next line of input.
     *
     * @return the next line, or {@code null} if no more input is available
     */
    String readLine();

    /**
     * Releases any underlying resources held by the input source.
     */
    @Override
    void close();
}
