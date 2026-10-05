package net.onelitefeather.butterfly.api.config;

import io.avaje.config.Configuration;
import org.slf4j.Logger;

import java.util.IllegalFormatException;
import java.util.Optional;

/**
 * Immutable settings shared by the Paper plugin and the Minestom extension.
 *
 * @param sortFormat    {@link String#format} pattern for the numeric team-name prefix that sorts the tab list
 * @param teamCollision whether players in the same team push each other (Minestom only)
 */
public record ButterflySettings(String sortFormat, boolean teamCollision) {

    public static final String SORT_FORMAT_KEY = "butterfly.teams.sort-format";
    public static final String TEAM_COLLISION_KEY = "butterfly.teams.collision";
    /** Key of the system property that configured the sort format before the settings file existed. */
    static final String LEGACY_SORT_FORMAT_KEY = "butterfly.format";

    private static final String DEFAULT_SORT_FORMAT = "%04d";
    private static final boolean DEFAULT_TEAM_COLLISION = false;

    public static ButterflySettings defaults() {
        return new ButterflySettings(DEFAULT_SORT_FORMAT, DEFAULT_TEAM_COLLISION);
    }

    /**
     * Reads the settings from the configuration. Absent keys take their default; a value that cannot be used
     * is replaced by the default and reported through the logger.
     */
    static ButterflySettings from(Configuration configuration, Logger logger) {
        return new ButterflySettings(
                readSortFormat(configuration, logger),
                readTeamCollision(configuration, logger)
        );
    }

    private static String readSortFormat(Configuration configuration, Logger logger) {
        String key = SORT_FORMAT_KEY;
        Optional<String> value = configuration.getOptional(key);
        if (value.isEmpty()) {
            key = LEGACY_SORT_FORMAT_KEY;
            value = configuration.getOptional(key);
        }
        if (value.isEmpty()) return DEFAULT_SORT_FORMAT;
        if (!canFormatSortId(value.get())) {
            logger.warn("Invalid value '{}' for {}, using the default '{}'", value.get(), key, DEFAULT_SORT_FORMAT);
            return DEFAULT_SORT_FORMAT;
        }
        return value.get();
    }

    private static boolean readTeamCollision(Configuration configuration, Logger logger) {
        Optional<String> value = configuration.getOptional(TEAM_COLLISION_KEY);
        if (value.isEmpty()) return DEFAULT_TEAM_COLLISION;
        if (value.get().equalsIgnoreCase("true")) return true;
        if (value.get().equalsIgnoreCase("false")) return false;
        logger.warn("Invalid value '{}' for {}, using the default '{}'", value.get(), TEAM_COLLISION_KEY, DEFAULT_TEAM_COLLISION);
        return DEFAULT_TEAM_COLLISION;
    }

    private static boolean canFormatSortId(String format) {
        try {
            String.format(format, 1);
            return true;
        } catch (IllegalFormatException e) {
            return false;
        }
    }
}
