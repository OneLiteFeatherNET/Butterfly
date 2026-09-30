package net.onelitefeather.butterfly.api;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.track.UserTrackEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

final class LuckPermsAPIImplementation implements LuckPermsAPI {
    /**
     * Resolved on every use instead of in a static initialiser: loading this class must not require LuckPerms
     * to be up yet, and it must follow LuckPerms being registered or unregistered.
     */
    static LuckPerms luckPerms() {
        return LuckPermsProvider.get();
    }

    static LuckPermsService LUCK_PERMS_SERVICE = new DummyLuckPermsService();

    static LuckPermsAPI INSTANCE = new LuckPermsAPIImplementation();

    static final Comparator<Group> GROUP_COMPARATOR = Comparator.comparing(group -> group.getWeight().orElse(-1), Comparator.reverseOrder());
    private final List<EventSubscription<?>> luckPermsEvents = new ArrayList<>();

    private LuckPermsAPIImplementation() {
    }

    @Override
    public Group getPrimaryGroup(UUID playerUUID) {
        User user = luckPerms().getUserManager().getUser(playerUUID);
        if (user == null) return LUCK_PERMS_SERVICE.getDefaultGroup();

        Group group = luckPerms().getGroupManager().getGroup(user.getPrimaryGroup());
        return (group != null) ? group : LUCK_PERMS_SERVICE.getDefaultGroup();
    }

    @Override
    public void subscribeEvents() {
        EventBus eventBus = luckPerms().getEventBus();
        this.luckPermsEvents.add(eventBus.subscribe(UserTrackEvent.class, event -> LUCK_PERMS_SERVICE.setDisplayName(event.getUser())));
    }

    @Override
    public void unsubscribeEvents() {
        this.luckPermsEvents.forEach(EventSubscription::close);
        this.luckPermsEvents.clear();
    }
}

