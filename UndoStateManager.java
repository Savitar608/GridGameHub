import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Generic, reusable undo manager based on full-state snapshots.
 * - Each player may undo ONCE per game.
 * - Only allows undo if the last recorded move belongs to that player.
 *
 * Type params:
 *   S = snapshot type (your game defines it)
 *   P = player type (e.g., String name or Player object)
 */
/**
 * Generic undo manager based on full-state snapshots. Supports one undo per
 * player per game and only allows undoing the last move when it belongs to
 * the requesting player.
 */
public final class UndoStateManager<S, P> {

    private static final class Entry<S, P> {
        final String playerId;
        final S snapshotBeforeMove;
        Entry(String playerId, S snapshotBeforeMove) {
            this.playerId = playerId;
            this.snapshotBeforeMove = snapshotBeforeMove;
        }
    }

    private final Function<P, String> playerIdFn;
    private Supplier<S> snapshotFn;
    private Consumer<S> restoreFn;

    private final Deque<Entry<S, P>> history = new ArrayDeque<>();
    private final Map<String, Boolean> usedUndo = new HashMap<>(); // per-player "used once"

    public UndoStateManager(Function<P, String> playerIdFn) {
        if (playerIdFn == null) throw new IllegalArgumentException("playerIdFn == null");
        this.playerIdFn = playerIdFn;
    }

    /** Provide functions for taking and restoring snapshots (call once per game setup). */
    public void setSnapshotFunctions(Supplier<S> snapshotFn, Consumer<S> restoreFn) {
        if (snapshotFn == null || restoreFn == null) throw new IllegalArgumentException("null snapshot/restore");
        this.snapshotFn = snapshotFn;
        this.restoreFn = restoreFn;
    }

    /** Clear history and per-player usage; call at the start of EACH new game. */
    public void resetForNewGame() {
        history.clear();
        usedUndo.clear();
    }

    /** Capture a snapshot BEFORE you apply a move by this player. */
    public void recordBeforeMove(P player) {
        if (snapshotFn == null) throw new IllegalStateException("snapshot/restore not set");
        String id = playerIdFn.apply(player);
        S snap = snapshotFn.get();
        history.push(new Entry<>(id, snap));
    }

    /** Try to undo the last move (must belong to this player and they must not have used undo yet). */
    public boolean undoLastFor(P player, Consumer<String> feedbackOut) {
        if (restoreFn == null) throw new IllegalStateException("snapshot/restore not set");
        String id = playerIdFn.apply(player);

        if (usedUndo.getOrDefault(id, false)) {
            if (feedbackOut != null) feedbackOut.accept("Undo already used for " + id + ".");
            return false;
        }
        if (history.isEmpty()) {
            if (feedbackOut != null) feedbackOut.accept("No moves to undo.");
            return false;
        }

        Entry<S, P> top = history.peek();
        if (!top.playerId.equals(id)) {
            if (feedbackOut != null) feedbackOut.accept("You can only undo your own last move.");
            return false;
        }

        history.pop();
        restoreFn.accept(top.snapshotBeforeMove);
        usedUndo.put(id, true);
        if (feedbackOut != null) feedbackOut.accept("Undone last move for " + id + ". (No more undos for this player.)");
        return true;
    }

    /** Remaining undo for player: 1 or 0. */
    public int remainingFor(P player) {
        String id = playerIdFn.apply(player);
        return usedUndo.getOrDefault(id, false) ? 0 : 1;
    }
}
