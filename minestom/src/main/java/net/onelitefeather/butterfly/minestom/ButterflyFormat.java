package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.luckperms.api.model.group.Group;
import net.minestom.server.color.TeamColor;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Turns the LuckPerms group of a player into the components Butterfly displays.
 * <p>
 * Chat and the name tag above a player go through the same {@link GroupFormat}, so the
 * color a player carries in chat is always the color of their scoreboard team.
 */
final class ButterflyFormat {

    /**
     * Parses chat written by a player. Deliberately limited to colors and decorations:
     * click, hover, insertion, selector, score, nbt and newline would otherwise let any
     * player forge chat lines or hand others a clickable command.
     * <p>
     * The tag set has to be built into the instance. Handing a resolver to
     * {@code deserialize} adds to the standard tags instead of replacing them, which
     * would leave every one of those tags available.
     */
    private static final MiniMessage CHAT_MINI_MESSAGE = MiniMessage.builder()
            .tags(TagResolver.resolver(
                    StandardTags.color(),
                    StandardTags.decorations(),
                    StandardTags.gradient(),
                    StandardTags.rainbow(),
                    StandardTags.transition(),
                    StandardTags.reset()
            ))
            .build();

    private ButterflyFormat() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param teamName    the scoreboard team of the group, or {@code null} if the group could not be resolved
     * @param prefix      the rendered group prefix, empty when the group defines none
     * @param teamColor   the color of the player name
     * @param displayName the prefix followed by the colored player name
     */
    record GroupFormat(@Nullable String teamName, @NotNull Component prefix, @NotNull TeamColor teamColor,
                       @NotNull Component displayName) {
    }

    /**
     * Renders a group for a player. A group without a prefix is formatted with an empty
     * one rather than skipped, so those players still get a team and a colored name.
     *
     * @param group    the primary group of the player, may be {@code null}
     * @param username the name of the player
     * @return the rendered format, never {@code null}
     */
    static @NotNull GroupFormat of(@Nullable Group group, @NotNull String username) {
        LuckPermsAPI api = LuckPermsAPI.luckPermsAPI();

        String rawPrefix = api.getGroupPrefix(group).orElse("");
        // Prefixes are configured by staff, so they keep the full MiniMessage tag set.
        Component prefix = rawPrefix.isEmpty() ? Component.empty() : MiniMessage.miniMessage().deserialize(rawPrefix);

        // TeamColor carries the matching NamedTextColor, so the name in chat and the name
        // above the player's head cannot end up on two different colors.
        TeamColor teamColor = teamColor(api.getTeamColorName(group));
        Component displayName = Component.text()
                .append(prefix)
                .append(Component.text(username, teamColor.textColor()))
                .build();

        return new GroupFormat(group != null ? api.getTeamName(group) : null, prefix, teamColor, displayName);
    }

    /**
     * Renders a chat message written by a player.
     *
     * @param rawMessage      the message as typed
     * @param allowFormatting whether the author may use MiniMessage formatting
     * @return the message, kept literal unless the author is allowed to format chat
     */
    static @NotNull Component chatMessage(@NotNull String rawMessage, boolean allowFormatting) {
        if (!allowFormatting) {
            return Component.text(rawMessage);
        }
        return CHAT_MINI_MESSAGE.deserialize(rawMessage);
    }

    /**
     * Joins a display name and a message into a chat line.
     */
    static @NotNull Component chatLine(@NotNull Component displayName, @NotNull Component message) {
        return Component.text()
                .append(displayName)
                .append(Component.text(": "))
                .append(message)
                .build();
    }

    /**
     * @param colorName a color name as it appears in {@link net.onelitefeather.butterfly.util.TeamColors}
     * @return the matching team color, white if Minestom does not know the name
     */
    static @NotNull TeamColor teamColor(@NotNull String colorName) {
        TeamColor teamColor = TeamColor.fromName(colorName);
        return teamColor != null ? teamColor : TeamColor.WHITE;
    }
}
