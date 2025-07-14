package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.onelitefeather.butterfly.api.LuckPermsAPI;

public final class Butterfly {

    public static Butterfly create() {
        return new Butterfly();
    }

    public void load() {
        LuckPermsAPI.setLuckPermsService(new MinestomLuckPermsService());
        LuckPermsAPI.luckPermsAPI().subscribeEvents();

        MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent.class, this::playerChat);
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, this::playerSpawn);
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
    }
}
