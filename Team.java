
/**
 * File: Team.java
 * Description: Represents a team in cooperative/competitive game modes. Holds
 *              identifying metadata allowing players to associate themselves
 *              with a shared group during gameplay.
 */

import java.util.Locale;
import java.util.Objects;

/**
 * Immutable value object capturing team identity.
 */
public final class Team {
    private final String name;
    private final String tag;
    private final String color;

    /**
     * Creates a team with the provided attributes.
     *
     * @param name  human friendly team name
     * @param tag   short identifier (e.g., initials or code)
     * @param color color descriptor for UI representation
     * @throws IllegalArgumentException when name or tag is blank
     */
    public Team(String name, String tag, String color) {
        this.name = sanitizeOrThrow(name, "Team name must not be blank");
        this.tag = sanitizeOrThrow(tag, "Team tag must not be blank");
        this.color = sanitize(color);
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "Team{name='" + name + "', tag='" + tag + "', color='" + color + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Team)) {
            return false;
        }
        Team other = (Team) o;
        return name.equalsIgnoreCase(other.name)
                && tag.equalsIgnoreCase(other.tag)
                && Objects.equals(color, other.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(Locale.ROOT), tag.toLowerCase(Locale.ROOT), color);
    }

    private static String sanitizeOrThrow(String value, String message) {
        String sanitized = sanitize(value);
        if (sanitized == null) {
            throw new IllegalArgumentException(message);
        }
        return sanitized;
    }

    private static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
