/**
 * Representation of a single cell in the Dots & Boxes grid.
 * Each piece knows its kind (dot, horizontal edge, vertical edge, or box)
 * and an owner character when applicable.
 */
public final class DotsAndBoxesPiece implements GamePiece {
    public enum Kind { DOT, H_EDGE, V_EDGE, BOX }

    private final Kind kind;
    private char owner;

    /**
     * Create a piece of the given kind with no owner.
     *
     * @param kind the kind of piece
     */
    public DotsAndBoxesPiece(Kind kind) { this(kind, (char)0); }

    /**
     * Create a piece with an explicit owner (0 = unowned).
     *
     * @param kind  the kind of piece
     * @param owner owner character (0 means empty)
     */
    public DotsAndBoxesPiece(Kind kind, char owner) {
        if (kind == null) throw new IllegalArgumentException("kind must not be null");
        this.kind = kind;
        this.owner = owner;
    }

    /** Returns the piece kind. */
    public Kind getKind() { return kind; }
    /** Returns the owner (0 if none). */
    public char getOwner() { return owner; }
    /** Sets the owner character for this piece. */
    public void setOwner(char owner) { this.owner = owner; }

    @Override public boolean isEmpty() {
        // Only edges are considered "empty" when owner==0; dots/boxes have other semantics.
        if (kind == Kind.H_EDGE || kind == Kind.V_EDGE) return owner == 0;
        return false;
    }

    @Override public String getDisplayToken() {
        // Return a short token for rendering in text UI
        switch (kind) {
            case DOT:    return "* ";
            case H_EDGE: return owner == 0 ? "  " : "--";
            case V_EDGE: return owner == 0 ? "  " : "| ";
            case BOX:    return owner == 0 ? "  " : (Character.toString(owner) + " ");
            default:     return "  ";
        }
    }
}
