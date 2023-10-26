package net.onelitefeather.butterfly.service;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.event.user.UserLoadEvent;
import net.luckperms.api.event.user.track.UserTrackEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.onelitefeather.butterfly.ButterflyPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LuckPermsService implements Listener {

    private final ButterflyPlugin plugin;
    private final LuckPerms luckPerms;

    private final List<EventSubscription<?>> luckPermsEvents;

    public LuckPermsService(@NotNull ButterflyPlugin plugin) {
        this.plugin = plugin;
        this.luckPermsEvents = new ArrayList<>();
        this.luckPerms = loadLuckPerms();
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @NotNull
    public String getPlayerGroupPrefix(@NotNull Player player) {
        Group group = getPrimaryGroup(player);
        String prefix = group.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getPrefix();
        return (prefix != null) ? prefix : "";
    }

    @NotNull
    public Group getPrimaryGroup(Player player) {

        User user = this.luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return getDefaultGroup();

        Group group = this.luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
        return (group != null) ? group : getDefaultGroup();
    }

    @NotNull
    public Group getDefaultGroup() {
        return Objects.requireNonNull(this.luckPerms.getGroupManager().getGroup("default"));
    }

    public void subscribeEvents() {
        EventBus eventBus = this.luckPerms.getEventBus();
        this.luckPermsEvents.add(eventBus.subscribe(UserTrackEvent.class, event -> setDisplayName(event.getUser())));
        this.luckPermsEvents.add(eventBus.subscribe(UserLoadEvent.class, event -> setDisplayName(event.getUser())));
        this.luckPermsEvents.add(eventBus.subscribe(UserDataRecalculateEvent.class, event -> setDisplayName(event.getUser())));
    }

    public void unsubscribeEvents() {
        this.luckPermsEvents.forEach(EventSubscription::close);
        this.luckPermsEvents.clear();
    }

    private void setDisplayName(@NotNull User user) {
        Player player = this.plugin.getServer().getPlayer(user.getUniqueId());
        if (player != null)
            player.displayName(MiniMessage.miniMessage().deserialize(getPlayerGroupPrefix(player) + player.getName()));
    }

    public LuckPerms getLuckPerms() {
        return this.luckPerms;
    }

    @Nullable
    private LuckPerms loadLuckPerms() {

        var server = this.plugin.getServer();
        if (!server.getPluginManager().isPluginEnabled("LuckPerms")) return null;

        var provider = server.getServicesManager().getRegistration(LuckPerms.class);
        if (provider == null) return null;
        return provider.getProvider();
    }

    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String displayName = getPlayerGroupPrefix(player) + player.getName();
        player.displayName(MiniMessage.miniMessage().deserialize(displayName));
    }

    @EventHandler
    public void handleChat(AsyncChatEvent event) {
        event.renderer((source, sourceDisplayName, message, viewer) -> Component.text()
                .append(sourceDisplayName)
                .append(Component.text(": "))
                .append(MiniMessage.miniMessage().deserialize(PlainTextComponentSerializer.plainText().serialize(message)))
                .build());
    }
}