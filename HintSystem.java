import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Reusable hint system with per-player limits (default 2 per game).
 * Generic over:  B = board type, P = player type, M = move type.
 */
/**
 * Small helper that uses a MoveStrategy to provide per-player hints with a
 * configurable limit (default 2 hints per player).
 */
public final class HintSystem<B, P, M> {
    private final MoveStrategy<B, P, M> strategy;
    private final Function<P, String> playerIdFn;
    private final int maxHintsPerPlayer;
    private final Map<String, Integer> used = new HashMap<>();

    /** Default: 2 hints per player. */
    public HintSystem(MoveStrategy<B, P, M> strategy, Function<P, String> playerIdFn) {
        this(strategy, playerIdFn, 2);
    }

    public HintSystem(MoveStrategy<B, P, M> strategy,
                      Function<P, String> playerIdFn,
                      int maxHintsPerPlayer) {
        if (strategy == null || playerIdFn == null) throw new IllegalArgumentException("null");
        if (maxHintsPerPlayer < 0) throw new IllegalArgumentException("maxHintsPerPlayer < 0");
        this.strategy = strategy;
        this.playerIdFn = playerIdFn;
        this.maxHintsPerPlayer = maxHintsPerPlayer;
    }

    /** Call this at the start of EACH new game. */
    public void resetForNewGame() { used.clear(); }

    /** Returns remaining hints for this player (0..max). */
    public int remainingFor(P player) {
        String id = playerIdFn.apply(player);
        int u = used.getOrDefault(id, 0);
        return Math.max(0, maxHintsPerPlayer - u);
    }

    /** Take 1 hint (throws if none left), returns the suggested move. */
    public M suggest(B board, P player) {
        String id = playerIdFn.apply(player);
        int u = used.getOrDefault(id, 0);
        if (u >= maxHintsPerPlayer) throw new IllegalStateException("No hints left for " + id);
        M move = strategy.chooseMove(board, player);
        used.put(id, u + 1);
        return move;
    }
}
