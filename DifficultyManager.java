
/**
 * CS611 - Object Oriented Design
 * Assignment 1 - Sliding Puzzle Game
 * 
 * File: DifficultyManager.java
 * Description: Encapsulates the management of difficulty levels for grid-based games.
 *              Provides utilities for defining, querying, and enumerating supported
 *              difficulty levels while preserving insertion order for display.
 * 
 * Features:
 * - Centralized difficulty registry with validation helpers
 * - Sorted enumeration of defined levels for consistent UI prompts
 * - Defensive copy accessors to protect internal state
 * - Contextual messaging based on selected difficulty
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Manages the collection of difficulty levels available in a grid-based game.
 * Uses a {@link LinkedHashMap} to preserve insertion order, allowing for
 * predictable display of options while supporting extensibility.
 */
/**
 * Registry for difficulty levels exposed to players. Preserves insertion order
 * for display and provides helpers for validation and messaging.
 */
public class DifficultyManager {
    private final Map<Integer, String> difficultyLevels;

    /**
     * Creates an empty difficulty registry ready to accept level definitions.
     */
    public DifficultyManager() {
        this.difficultyLevels = new LinkedHashMap<>();
    }

    /**
     * Removes all configured difficulty levels.
     */
    public void clear() {
        difficultyLevels.clear();
    }

    /**
     * Adds or updates a difficulty level.
     *
     * @param level the numeric difficulty identifier (must be positive)
     * @param name  the human-readable name for the level
     */
    public void addDifficultyLevel(int level, String name) {
        if (level < 1) {
            throw new IllegalArgumentException("Difficulty level must be a positive integer.");
        }
        String displayName = (name == null || name.trim().isEmpty()) ? ("Level " + level) : name.trim();
        difficultyLevels.put(level, displayName);
    }

    /**
     * Retrieves the display name for a difficulty level.
     *
     * @param level the difficulty level
     * @return the configured name, or "Unknown" if none exists
     */
    public String getDifficultyName(int level) {
        return difficultyLevels.getOrDefault(level, "Unknown");
    }

    /**
     * Checks whether a difficulty level has been defined.
     *
     * @param level the difficulty level to verify
     * @return {@code true} if the level exists, otherwise {@code false}
     */
    public boolean isValidDifficultyLevel(int level) {
        return difficultyLevels.containsKey(level);
    }

    /**
     * Provides the smallest configured difficulty level.
     *
     * @return the minimum level value
     */
    public int getMinDifficultyLevel() {
        return difficultyLevels.keySet().stream()
                .min(Integer::compareTo)
                .orElseThrow(() -> new IllegalStateException("No difficulty levels configured."));
    }

    /**
     * Provides the largest configured difficulty level.
     *
     * @return the maximum level value
     */
    public int getMaxDifficultyLevel() {
        return difficultyLevels.keySet().stream()
                .max(Integer::compareTo)
                .orElseThrow(() -> new IllegalStateException("No difficulty levels configured."));
    }

    /**
     * Returns a sorted list of all configured difficulty levels.
     *
     * @return sorted list of level identifiers
     */
    public List<Integer> getSortedDifficultyLevels() {
        List<Integer> levels = new ArrayList<>(difficultyLevels.keySet());
        Collections.sort(levels);
        return levels;
    }

    /**
     * Displays a difficulty-specific message to the player based on the level
     * selected.
     *
     * @param difficultyLevel the level for which to display the message
     * @param outputService   destination for the explanatory messages
     */
    public void displayDifficultyMessage(int difficultyLevel, OutputService outputService) {
        Objects.requireNonNull(outputService, "outputService must not be null");

        String difficultyName = getDifficultyName(difficultyLevel);

        if (difficultyLevel >= 4) {
            outputService.println("🔥 EXTREME DIFFICULTY ACTIVATED! 🔥");
            outputService.println("Warning: " + difficultyName + " mode is for seasoned puzzle masters!");
            outputService.println(
                    "Tip: Take your time and think several moves ahead. Consider using pen and paper to track your strategy.");
        } else if (difficultyLevel == 3) {
            outputService.println("Warning: " + difficultyName + " mode can be quite challenging!");
            outputService.println("Tip: Plan your moves ahead and try to visualize the solution.");
        } else if (difficultyLevel == 1) {
            outputService.println("Perfect for beginners! Take your time to learn the game mechanics.");
        }
    }

    /**
     * Returns a defensive copy of the current difficulty mapping.
     *
     * @return copy of level-to-name map
     */
    public Map<Integer, String> getDifficultyLevelsSnapshot() {
        return new LinkedHashMap<>(difficultyLevels);
    }

    /**
     * Returns a human-readable representation of the configured difficulty state.
     *
     * @return string describing the known difficulty levels
     */
    @Override
    public String toString() {
        return "DifficultyManager" + Objects.toString(difficultyLevels);
    }
}
