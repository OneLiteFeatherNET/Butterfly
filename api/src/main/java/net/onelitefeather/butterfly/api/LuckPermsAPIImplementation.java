package net.onelitefeather.butterfly.api;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.util.Optional;
import java.util.UUID;

final class LuckPermsAPIImplementation implements LuckPermsAPI {

    private static final Optional<LuckPermsService> SERVICE = Services.service(LuckPermsService.class);
    static final LuckPermsService LUCK_PERMS_SERVICE = SERVICE.orElseThrow();
    static final LuckPerms LUCK_PERMS = LUCK_PERMS_SERVICE.getLuckPerms();

    static LuckPermsAPI INSTANCE = new LuckPermsAPIImplementation();

    private LuckPermsAPIImplementation() {
    }

    @Override
    public Group getPrimaryGroup(UUID playerUUID) {
        User user = LUCK_PERMS.getUserManager().getUser(playerUUID);
        if (user == null) return LUCK_PERMS_SERVICE.getDefaultGroup();

        Group group = LUCK_PERMS.getGroupManager().getGroup(user.getPrimaryGroup());
        return (group != null) ? group : LUCK_PERMS_SERVICE.getDefaultGroup();
    }
}

