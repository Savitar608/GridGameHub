/**
 * Enumeration of the four possible edges surrounding a single box in the
 * Dots and Boxes game. Provides utility helpers for navigating to the opposite
 * edge which is shared by an adjacent box.
 */
public enum DotsAndBoxesEdge {
    TOP,
    RIGHT,
    BOTTOM,
    LEFT;

    /**
     * Returns the opposing edge that would be shared by an adjacent box.
     *
     * @return opposite edge
     */
    public DotsAndBoxesEdge opposite() {
        switch (this) {
            case TOP:
                return BOTTOM;
            case BOTTOM:
                return TOP;
            case LEFT:
                return RIGHT;
            case RIGHT:
            default:
                return LEFT;
        }
    }
}
