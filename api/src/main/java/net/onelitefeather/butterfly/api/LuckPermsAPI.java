package net.onelitefeather.butterfly.api;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
     * @return the primary group
     */
    Group getPrimaryGroup(UUID playerUUID);

    /**
     * Fetch the prefix from luck perms
     * @param group be receiving the prefix
     * @return the prefix of the group
     */
    default Optional<String> getGroupPrefix(Group group) {
        String prefix = group.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getPrefix();
        if (prefix != null) {
            return Optional.of(prefix);
        }
        return Optional.empty();
    }

    /**
     * Resolves the prefix LuckPerms itself shows for a player: the highest-priority prefix across all of the
     * user's groups (inherited) and the user's own nodes, in the user's current contexts. This is not necessarily
     * the prefix of the primary group.
     *
     * @param user the player's LuckPerms user
     * @return the effective prefix, empty if the user has none
     */
    default Optional<String> getPlayerPrefix(User user) {
        return Optional.ofNullable(user.getCachedData().getMetaData().getPrefix());
    }

    /**
     * Same as {@link #getPlayerPrefix(User)} for a player identified by UUID. If the user is not loaded, the prefix
     * of the default group is used, matching the fallback of {@link #getPrimaryGroup(UUID)}.
     *
     * @param playerUUID the player's UUID
     * @return the effective prefix, empty if there is none
     */
    default Optional<String> getPlayerPrefix(UUID playerUUID) {
        User user = getUser(playerUUID);
        if (user != null) {
            return getPlayerPrefix(user);
        }
        return getGroupPrefix(getPrimaryGroup(playerUUID));
    }

    /**
     * Checks a permission against the user's cached permission data, using the user's own contextual query options.
     * This reads cached data only, so it is safe to call from any thread.
     *
     * @param user       the player's LuckPerms user, may be null
     * @param permission the permission node
     * @return true if the permission is granted, false if it is not or the user is null
     */
    default boolean hasPermission(User user, String permission) {
        if (user == null) return false;
        return user.getCachedData().getPermissionData(user.getQueryOptions())
                .queryPermission(permission).result().asBoolean();
    }

    /**
     * Same as {@link #hasPermission(User, String)} for a player identified by UUID.
     *
     * @param playerUUID the player's UUID
     * @param permission the permission node
     * @return true if the permission is granted, false if it is not or the user is not loaded
     */
    default boolean hasPermission(UUID playerUUID, String permission) {
        return hasPermission(getUser(playerUUID), permission);
    }

    default User getUser(UUID uuid) {
        return LuckPermsProvider.get().getUserManager().getUser(uuid);
    }

    default int getGroupSortId(Group group) {

        List<Group> sortedGroups = LuckPermsAPIImplementation.luckPerms().getGroupManager()
                .getLoadedGroups().stream().sorted(LuckPermsAPIImplementation.GROUP_COMPARATOR).toList();

        return sortedGroups.indexOf(group) + 1;
    }

    default void setDisplayName(User user) {
        LuckPermsAPIImplementation.LUCK_PERMS_SERVICE.setDisplayName(user);
    }

    void subscribeEvents();

    void unsubscribeEvents();

}
