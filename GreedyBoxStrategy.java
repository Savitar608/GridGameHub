/** Greedy hint strategy for Dots & Boxes. */
/**
 * Greedy strategy that prefers moves completing a box, otherwise returns the
 * first available edge. Used by the hint system to suggest moves.
 */
public final class GreedyBoxStrategy implements MoveStrategy<Grid<DotsAndBoxesPiece>, String, DotsAndBoxesMove> {
    @Override
    public DotsAndBoxesMove chooseMove(Grid<DotsAndBoxesPiece> grid, String playerName) {
        int R = grid.getRows();
        int C = grid.getCols();

        // 1) Prefer completing edges: scan the whole grid and pick any edge
        // that, if claimed, would complete a box.
        for (int r = 0; r < R; r++) for (int c = 0; c < C; c++) {
            if (r % 2 == 0 && c % 2 == 1) { // horizontal edge
                DotsAndBoxesPiece e = grid.getPiece(r, c);
                if (e.isEmpty() && completesABox(grid, r, c, true)) {
                    int dotRow = r / 2;
                    int rightDot = (c + 1) / 2;
                    return new DotsAndBoxesMove(DotsAndBoxesMove.Kind.H, dotRow, rightDot);
                }
            }
            if (r % 2 == 1 && c % 2 == 0) { // vertical edge
                DotsAndBoxesPiece e = grid.getPiece(r, c);
                if (e.isEmpty() && completesABox(grid, r, c, false)) {
                    int bottomDot = (r + 1) / 2;
                    int dotCol = c / 2;
                    return new DotsAndBoxesMove(DotsAndBoxesMove.Kind.V, bottomDot, dotCol);
                }
            }
        }

        // 2) Fallback: return the first empty edge found (row-major scan)
        for (int r = 0; r < R; r++) for (int c = 0; c < C; c++) {
            if ((r % 2 == 0 && c % 2 == 1) || (r % 2 == 1 && c % 2 == 0)) {
                DotsAndBoxesPiece e = grid.getPiece(r, c);
                if (e.isEmpty()) {
                    if (r % 2 == 0) { // horizontal
                        int dotRow = r / 2;
                        int rightDot = (c + 1) / 2;
                        return new DotsAndBoxesMove(DotsAndBoxesMove.Kind.H, dotRow, rightDot);
                    } else { // vertical
                        int bottomDot = (r + 1) / 2;
                        int dotCol = c / 2;
                        return new DotsAndBoxesMove(DotsAndBoxesMove.Kind.V, bottomDot, dotCol);
                    }
                }
            }
        }

        return null; // no move available
    }

    /**
     * Determines whether claiming the edge at (r,c) would complete any adjacent
     * box. The method checks both possible boxes that share the edge.
     */
    private boolean completesABox(Grid<DotsAndBoxesPiece> grid, int r, int c, boolean horizontal) {
        int R = grid.getRows(), C = grid.getCols();
        if (horizontal) {
            // Check the box above the horizontal edge
            if (r - 1 >= 1 && c - 1 >= 1 && c + 1 <= C - 2) {
                if (isClaimed(grid, r - 1, c) && isClaimed(grid, r, c - 1) && isClaimed(grid, r, c + 1)) return true;
            }
            // Check the box below the horizontal edge
            if (r + 1 <= R - 2 && c - 1 >= 1 && c + 1 <= C - 2) {
                if (isClaimed(grid, r + 1, c) && isClaimed(grid, r, c - 1) && isClaimed(grid, r, c + 1)) return true;
            }
        } else {
            // Check the box to the left of the vertical edge
            if (c - 1 >= 1 && r - 1 >= 1 && r + 1 <= R - 2) {
                if (isClaimed(grid, r, c - 1) && isClaimed(grid, r - 1, c) && isClaimed(grid, r + 1, c)) return true;
            }
            // Check the box to the right of the vertical edge
            if (c + 1 <= C - 2 && r - 1 >= 1 && r + 1 <= R - 2) {
                if (isClaimed(grid, r, c + 1) && isClaimed(grid, r - 1, c) && isClaimed(grid, r + 1, c)) return true;
            }
        }
        return false;
    }

    /** Helper that returns true when the piece at (r,c) is already claimed. */
    private boolean isClaimed(Grid<DotsAndBoxesPiece> grid, int r, int c) {
        DotsAndBoxesPiece p = grid.getPiece(r, c);
        return !p.isEmpty();
    }
}
