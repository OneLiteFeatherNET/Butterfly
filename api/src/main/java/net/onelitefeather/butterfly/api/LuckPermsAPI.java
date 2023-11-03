package net.onelitefeather.butterfly.api;

import net.luckperms.api.model.group.Group;
import net.luckperms.api.query.QueryOptions;

import java.util.UUID;

/**
 * Luckperms API for internal access
 */
public sealed interface LuckPermsAPI permits LuckPermsAPIImplementation{

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
    default String getGroupPrefix(Group group) {
        String prefix = group.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getPrefix();
        if (prefix != null) {
            return prefix;
        }
        throw new NullPointerException("Group prefix is null");
    }

}
