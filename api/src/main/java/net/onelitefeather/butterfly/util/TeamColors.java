package net.onelitefeather.butterfly.util;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Color names usable as a scoreboard team color.
 * <p>
 * The names are fixed by the protocol and deliberately kept free of any Adventure
 * type: the Paper module runs against Adventure 4 while the Minestom module runs
 * against Adventure 5, so each platform maps these names onto its own color type.
 */
public final class TeamColors {

    private TeamColors() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * The sixteen vanilla color names, in protocol order.
     * <p>
     * The order decides which color wins when a group carries more than one color
     * permission, so it is spelled out here on purpose: Adventure's
     * {@code NamedTextColor.NAMES} is keyed by a hash and iterates in an order that
     * differs between the Adventure versions the two platforms run against.
     */
    public static final List<String> NAMES = List.of(
            "black", "dark_blue", "dark_green", "dark_aqua",
            "dark_red", "dark_purple", "gold", "gray",
            "dark_gray", "blue", "green", "aqua",
            "red", "light_purple", "yellow", "white"
    );

    /**
     * Fallback color used when a group neither carries a color permission nor a
     * colored prefix.
     */
    public static final String DEFAULT = "white";

    /**
     * Matches a single MiniMessage tag that is not escaped with a backslash.
     */
    private static final Pattern TAG = Pattern.compile("(?<!\\\\)<([^<>]*)>");

    /**
     * Aliases MiniMessage accepts for an explicit color tag, as in {@code <color:red>}.
     */
    private static final List<String> COLOR_TAG_ALIASES = List.of("color", "colour", "c");

    /**
     * Resolves the color a MiniMessage string leaves in effect at its end.
     * <p>
     * A group prefix such as {@code <gray>[<red>Admin<gray>] } is what the player
     * name inherits in chat, so the trailing color is also the right team color to
     * show above the player's head. Colors that cannot be expressed as a team color
     * (hex, gradients, rainbows) are ignored, and a closing color tag or
     * {@code <reset>} clears whatever was resolved before it.
     *
     * @param miniMessage the MiniMessage string to inspect, may be {@code null}
     * @return the trailing color name, or empty if the string leaves no named color
     */
    public static Optional<String> trailingColor(String miniMessage) {
        if (miniMessage == null || miniMessage.isEmpty()) return Optional.empty();

        String color = null;
        Matcher matcher = TAG.matcher(miniMessage);
        while (matcher.find()) {
            String tag = matcher.group(1).trim().toLowerCase(Locale.ROOT);
            if (tag.isEmpty()) continue;

            if (tag.startsWith("/")) {
                if (closesColor(tag.substring(1))) color = null;
                continue;
            }
            if (tag.equals("reset")) {
                color = null;
                continue;
            }

            String name = stripColorAlias(tag);
            if (NAMES.contains(name)) color = name;
        }

        return Optional.ofNullable(color);
    }

    /**
     * @return whether the body of a closing tag ends a named color
     */
    private static boolean closesColor(String tag) {
        String name = stripColorAlias(tag.trim());
        return NAMES.contains(name) || COLOR_TAG_ALIASES.contains(name);
    }

    /**
     * Reduces {@code color:red}, {@code colour:red} and {@code c:red} to {@code red},
     * leaving every other tag untouched.
     */
    private static String stripColorAlias(String tag) {
        int separator = tag.indexOf(':');
        if (separator < 0) return tag;
        if (!COLOR_TAG_ALIASES.contains(tag.substring(0, separator))) return tag;
        return tag.substring(separator + 1);
    }
}
