package net.onelitefeather.butterfly.api;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.onelitefeather.butterfly.util.Constants;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Luckperms API for internal access
 */
public sealed interface LuckPermsAPI permits LuckPermsAPIImplementation {

    static LuckPermsAPI luckPermsAPI() {
        return LuckPermsAPIImplementation.INSTANCE;
    }

    static void setLuckPermsService(LuckPermsService service) {
        LuckPermsAPIImplementation.LUCK_PERMS_SERVICE = service;
    }

    /**
     * Gets the primary group of a player
     * @param playerUUID to fetch the group
     * @return the primary group, or {@code null} if neither the player's group nor the
     *         configured default group could be resolved
     */
    Group getPrimaryGroup(UUID playerUUID);

    /**
     * Fetch the prefix from luck perms
     * @param group be receiving the prefix
     * @return the prefix of the group
     */
    default Optional<String> getGroupPrefix(Group group) {
        return LuckPermsData.prefix(group);
    }

    default User getUser(UUID uuid) {
        return LuckPermsProvider.get().getUserManager().getUser(uuid);
    }

    /**
     * Loads a player that LuckPerms has not cached yet. Used when a player reaches the
     * server before their profile finished loading, so their team is still applied.
     *
     * @param uuid the player to load
     * @return a future completing with the loaded user
     */
    default CompletableFuture<User> loadUser(UUID uuid) {
        return LuckPermsProvider.get().getUserManager().loadUser(uuid);
    }

    default int getGroupSortId(Group group) {

        List<Group> sortedGroups = LuckPermsAPIImplementation.LUCK_PERMS.getGroupManager()
                .getLoadedGroups().stream().sorted(LuckPermsAPIImplementation.GROUP_COMPARATOR).toList();

        return sortedGroups.indexOf(group) + 1;
    }

    /**
     * Builds the scoreboard team name for a group. The numeric prefix orders the
     * teams by group weight, so the tab list follows the permission hierarchy.
     *
     * @param group the group to build the team name for
     * @return the team name
     */
    default String getTeamName(Group group) {
        return String.format(Constants.TEAM_SORT_FORMAT, getGroupSortId(group)) + group.getName();
    }

    /**
     * Resolves the team color of a group.
     * <p>
     * An explicit {@code color.<weight>.<name>} permission wins. Groups that only
     * define a colored prefix fall back to the color their prefix leaves in effect,
     * so the name above a player's head matches the name shown in chat without
     * needing a second permission node.
     *
     * @param group the group to resolve the color for
     * @return the color name, never {@code null}
     */
    default String getTeamColorName(Group group) {
        return LuckPermsData.teamColorName(group);
    }

    /**
     * Whether a player may use MiniMessage formatting in chat.
     * <p>
     * Formatting is allowed unless {@code butterfly.chat.format} is explicitly denied, so a
     * server that never set the node keeps the colored chat it had. Revoke it with a negated
     * node, for example {@code lp group default permission set butterfly.chat.format false}.
     * The tags a player can reach are restricted either way; the ones that let a player forge
     * a chat line or hand out a clickable command are never available.
     *
     * @param playerUUID the player to check
     * @return whether the player may use MiniMessage formatting in chat
     */
    default boolean canFormatChat(UUID playerUUID) {
        return LuckPermsData.canFormatChat(getUser(playerUUID));
    }

    default void setDisplayName(User user) {
        if (user == null) return;
        LuckPermsAPIImplementation.LUCK_PERMS_SERVICE.setDisplayName(user);
    }

    void subscribeEvents();

    void unsubscribeEvents();

}
