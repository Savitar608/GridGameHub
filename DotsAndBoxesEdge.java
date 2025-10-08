/**
 * File: DotsAndBoxesEdge.java
 * Description: Enumeration capturing the four edges around a Dots and Boxes cell and
 *              providing helpers for navigating to opposing edges.
 */

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
     * Returns the edge opposite the current enum constant, corresponding to the
     * shared edge on the neighboring box.
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
