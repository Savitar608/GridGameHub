/** A printable move for Dots & Boxes, matching your input format. */
/**
 * Small value object representing a user's move in Dots & Boxes.
 */
public final class DotsAndBoxesMove {
    public enum Kind { H, V }  // horizontal / vertical
    /** Kind of the move (horizontal or vertical) */
    public final Kind kind;
    /** Coordinate semantics:
     *  - For H: r is dot row (0..R), c is RIGHT dot column (1..C)
     *  - For V: r is bottom dot row (1..R), c is dot column (0..C)
     */
    public final int r;  
    public final int c;

    public DotsAndBoxesMove(Kind kind, int r, int c) {
        this.kind = kind; this.r = r; this.c = c;
    }

    @Override public String toString() {
        return (kind == Kind.H ? "h " : "v ") + r + " " + c;
    }
}
