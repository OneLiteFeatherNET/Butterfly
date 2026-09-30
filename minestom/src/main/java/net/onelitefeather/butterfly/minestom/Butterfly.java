package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Butterfly {

    private final EventNode<Event> parent;
    private @Nullable EventNode<Event> node;
    private @Nullable MinestomLuckPermsService service;

    private Butterfly(@NotNull EventNode<Event> parent) {
        this.parent = parent;
    }

    public static Butterfly create() {
        return new Butterfly(MinecraftServer.getGlobalEventHandler());
    }

    /**
     * Creates an instance whose listeners live on a child node of the given parent.
     */
    static Butterfly create(@NotNull EventNode<Event> parent) {
        return new Butterfly(parent);
    }

    public void load() {
        service = new MinestomLuckPermsService();
        LuckPermsAPI.setLuckPermsService(service);
        LuckPermsAPI.luckPermsAPI().subscribeEvents();

        node = EventNode.all("butterfly");
        node.addListener(PlayerChatEvent.class, this::playerChat);
        node.addListener(PlayerSpawnEvent.class, this::playerSpawn);
        parent.addChild(node);
    }

    private void playerSpawn(PlayerSpawnEvent playerSpawnEvent) {
        Player player = playerSpawnEvent.getPlayer();
        LuckPermsAPI.luckPermsAPI().setDisplayName(LuckPermsAPI.luckPermsAPI().getUser(player.getUuid()));
    }

    private void playerChat(PlayerChatEvent playerChatEvent) {
        Player player = playerChatEvent.getPlayer();
        var group = LuckPermsAPI.luckPermsAPI().getPrimaryGroup(player.getUuid());

        var prefixOptional = LuckPermsAPI.luckPermsAPI().getGroupPrefix(group);
        if(prefixOptional.isEmpty()) return;
        var prefix = prefixOptional.get();

        String displayName = prefix + player.getUsername();
        playerChatEvent.setFormattedMessage(Component.text()
                .append(MiniMessage.miniMessage().deserialize(displayName))
                .append(Component.text(": "))
                .append(MiniMessage.miniMessage().deserialize(playerChatEvent.getRawMessage()))
                .build());
    }

    public void terminate() {
        LuckPermsAPI.luckPermsAPI().unsubscribeEvents();
        if (node != null) {
            parent.removeChild(node);
            node = null;
        }
        if (service != null) {
            service.removeCreatedTeams();
            service = null;
        }
    }
}
