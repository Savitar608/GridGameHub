/**
 * Strategy interface used by hint systems to select a recommended move.
 *
 * @param <B> board type
 * @param <P> player identifier type
 * @param <M> move representation type
 */
public interface MoveStrategy<B, P, M> {
    /**
     * Choose a move for the given player given the current board state.
     *
     * @param board  current board
     * @param player player for whom to choose a move
     * @return selected move, or {@code null} if no move available
     */
    M chooseMove(B board, P player);
}
