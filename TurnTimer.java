import java.util.concurrent.TimeUnit;

/**
 * Simple per-turn timer that measures how long a player takes for their action.
 * Usage:
 *   timer.start(currentPlayerId);
 *   ... read input / process ...
 *   long ms = timer.stop(currentPlayerId);   // returns elapsed milliseconds
 *
 * Use cancel() to discard a timing if the input wasn't an actual move (e.g., 'hint'/'undo'/invalid).
 */
/**
 * Lightweight per-turn timer used to measure how long a player takes for their
 * move. Supports start/stop and cancellation for non-move commands.
 */
public final class TurnTimer<P> {
    private boolean running = false;
    private long    t0Nanos = 0L;
    private P       current = null;

    public void reset() {
        running = false;
        t0Nanos = 0L;
        current = null;
    }

    /** Start timing for the given player. If already running, restarts. */
    public void start(P player) {
        current = player;
        running = true;
        t0Nanos = System.nanoTime();
    }

    /**
     * Stop timing for the given player and return elapsed milliseconds.
     * If not running or player doesn't match current, returns 0.
     */
    public long stop(P player) {
        if (!running) return 0L;
        if (current != null && player != null && !current.equals(player)) {
            // different player tried to stop; ignore and keep state intact
            return 0L;
        }
        long elapsed = System.nanoTime() - t0Nanos;
        running = false;
        current = null;
        return TimeUnit.NANOSECONDS.toMillis(elapsed);
    }

    /** Discard the current measurement (use for non-move commands like 'hint'/'undo' or invalid input). */
    public void cancel() {
        running = false;
        current = null;
        t0Nanos = 0L;
    }

    /** Is the timer currently running? */
    public boolean isRunning() { return running; }
}
