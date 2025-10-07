/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 *
 * File: Team.java
 * Description: Immutable value object encapsulating team metadata shared by players,
 *              including name, short identifier, colors, and optional motto.
 */

import java.util.Objects;

/**
 * Immutable value object encapsulating lightweight team details for players.
 */
public final class Team {
    private static final String DEFAULT_TEAM_NAME = "Unnamed Team";
    private static final String DEFAULT_TEAM_TAG = "TEAM";

    private final String name;
    private final String tag;
    private final String color;

    /**
     * Creates a team with the provided name and identifier.
     *
     * @param name readable team name
     * @param tag  short identifier or acronym associated with the team
     */
    public Team(String name, String tag) {
        this(name, tag, null, null);
    }

    /**
     * Creates a team with colour palette and motto metadata.
     *
     * @param name   readable team name
     * @param tag    short identifier or acronym associated with the team
     * @param colors optional colour palette description
     * @param motto  optional team motto or slogan
     */
    public Team(String name, String tag, String colors, String motto) {
        this.name = sanitize(name, DEFAULT_TEAM_NAME);
        this.tag = sanitize(tag, DEFAULT_TEAM_TAG);
        this.color = sanitizeNullable(colors);
    }

    /**
     * @return configured team name
     */
    public String getName() {
        return name;
    }

    /**
     * @return short identifier or acronym representing the team
     */
    public String getTag() {
        return tag;
    }

    /**
     * @return colour palette description, or {@code null} if not provided
     */
    public String getColors() {
        return color;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Team)) {
            return false;
        }
        Team other = (Team) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(tag, other.tag)
                && Objects.equals(color, other.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tag, color);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Team{name='")
                .append(name)
                .append("', tag='")
                .append(tag)
                .append("'");

        if (color != null && !color.isEmpty()) {
            builder.append(", color='").append(color).append("'");
        }
        builder.append("}");
        return builder.toString();
    }

    private static String sanitize(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private static String sanitizeNullable(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }
}
