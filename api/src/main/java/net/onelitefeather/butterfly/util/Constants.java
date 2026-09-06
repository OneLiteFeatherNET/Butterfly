package net.onelitefeather.butterfly.util;

public final class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Permission node granting a group an explicit team color, formatted with the
     * group weight and the color name, e.g. {@code color.100.red}.
     */
    public static final String TEAM_COLOR_PERMISSION = "color.%s.%s";

    /**
     * Permission node deciding whether a player may use MiniMessage formatting in chat.
     * Formatting is allowed unless the node is explicitly denied.
     */
    public static final String CHAT_FORMATTING_PERMISSION = "butterfly.chat.format";

    /**
     * Numeric prefix of a scoreboard team name. Teams are ordered by this prefix
     * client side, so it has to be zero padded to sort lexicographically.
     */
    public static final String TEAM_SORT_FORMAT = System.getProperty("butterfly.format", "%04d");
}
