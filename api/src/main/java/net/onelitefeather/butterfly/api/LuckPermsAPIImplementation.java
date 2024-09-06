package net.onelitefeather.butterfly.api;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.event.user.UserLoadEvent;
import net.luckperms.api.event.user.track.UserTrackEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

final class LuckPermsAPIImplementation implements LuckPermsAPI {
    static final LuckPerms LUCK_PERMS = LuckPermsProvider.get();

    static LuckPermsService LUCK_PERMS_SERVICE = new DummyLuckPermsService();

    static LuckPermsAPI INSTANCE = new LuckPermsAPIImplementation();

    static final Comparator<Group> GROUP_COMPARATOR = Comparator.comparing(group -> group.getWeight().orElse(-1), Comparator.reverseOrder());
    private final List<EventSubscription<?>> luckPermsEvents = new ArrayList<>();

    private LuckPermsAPIImplementation() {
    }

    @Override
    public Group getPrimaryGroup(UUID playerUUID) {
        User user = LUCK_PERMS.getUserManager().getUser(playerUUID);
        if (user == null) return LUCK_PERMS_SERVICE.getDefaultGroup();

        Group group = LUCK_PERMS.getGroupManager().getGroup(user.getPrimaryGroup());
        return (group != null) ? group : LUCK_PERMS_SERVICE.getDefaultGroup();
    }

    @Override
    public void subscribeEvents() {
        EventBus eventBus = LUCK_PERMS.getEventBus();
        this.luckPermsEvents.add(eventBus.subscribe(UserTrackEvent.class, event -> LUCK_PERMS_SERVICE.setDisplayName(event.getUser())));
        this.luckPermsEvents.add(eventBus.subscribe(UserLoadEvent.class, event -> LUCK_PERMS_SERVICE.setDisplayName(event.getUser())));
        this.luckPermsEvents.add(eventBus.subscribe(UserDataRecalculateEvent.class, event -> LUCK_PERMS_SERVICE.setDisplayName(event.getUser())));
    }

    @Override
    public void unsubscribeEvents() {
        this.luckPermsEvents.forEach(EventSubscription::close);
        this.luckPermsEvents.clear();
    }
}

