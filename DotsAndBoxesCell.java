

import java.util.EnumMap;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents a single box in the Dots and Boxes grid. Tracks which edges have
 * been drawn and which player, if any, has claimed the box.
 */
public final class DotsAndBoxesCell implements GamePiece {
    private final EnumMap<DotsAndBoxesEdge, String> edges;
    private Player owner;

    public DotsAndBoxesCell() {
        this.edges = new EnumMap<>(DotsAndBoxesEdge.class);
        this.owner = null;
    }

    /**
     * Attempts to add the specified edge. Returns {@code true} when the edge was
     * not previously present and is now registered.
     *
     * @param edge  edge to add
     * @param color color code controlling how the edge is rendered
     * @return {@code true} when edge state changed
     */
    public boolean addEdge(DotsAndBoxesEdge edge, String color) {
        Objects.requireNonNull(edge, "edge must not be null");
        if (edges.containsKey(edge)) {
            return false;
        }
        edges.put(edge, color == null ? "" : color);
        return true;
    }

    /**
     * Checks whether the supplied edge has been drawn for this box.
     *
     * @param edge edge to inspect
     * @return {@code true} when the edge exists
     */
    public boolean hasEdge(DotsAndBoxesEdge edge) {
        return edges.containsKey(edge);
    }

    /**
     * Retrieves the color associated with the supplied edge if present.
     *
     * @param edge edge to inspect
     * @return ANSI color code or {@code null} when no edge exists
     */
    public String getEdgeColor(DotsAndBoxesEdge edge) {
        return edges.get(edge);
    }

    /**
     * Marks the box as claimed by the provided player when all edges are
     * present and no previous owner exists.
     *
     * @param player claiming player
     * @return {@code true} if the claim succeeded
     */
    public boolean claim(Player player) {
        if (owner != null || !isCompleted()) {
            return false;
        }
        owner = Objects.requireNonNull(player, "player must not be null");
        return true;
    }

    /**
     * @return the player who currently owns the box, or {@code null} if
     *         unclaimed
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * Determines whether all four edges for this box have been drawn.
     *
     * @return {@code true} when the box is complete
     */
    public boolean isCompleted() {
        return edges.size() == 4;
    }

    /**
     * Counts the number of edges currently drawn.
     *
     * @return edge count between 0 and 4
     */
    public int getEdgeCount() {
        return edges.size();
    }

    @Override
    public String getDisplayToken() {
        if (owner != null) {
            return owner.getTeamTag()
                    .filter(tag -> !tag.isEmpty())
                    .map(tag -> tag.substring(0, Math.min(2, tag.length())).toUpperCase(Locale.ROOT))
                    .orElseGet(() -> owner.getName().substring(0, Math.min(2, owner.getName().length()))
                            .toUpperCase(Locale.ROOT));
        }
        return Integer.toString(getEdgeCount());
    }

    @Override
    public boolean isEmpty() {
        return owner == null;
    }
}
