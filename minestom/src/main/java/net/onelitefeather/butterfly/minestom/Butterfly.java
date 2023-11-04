package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extensions.Extension;
import net.onelitefeather.butterfly.api.LuckPermsAPI;

public class Butterfly extends Extension {
    @Override
    public void initialize() {
        LuckPermsAPI.setLuckPermsService(new MinestomLuckPermsService());
        LuckPermsAPI.luckPermsAPI().subscribeEvents();

        MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent.class, this::playerChat);
        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, this::playerLogin);
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, this::playerSpawn);
    }

    private void playerSpawn(PlayerSpawnEvent playerSpawnEvent) {
        Player player = playerSpawnEvent.getPlayer();
        LuckPermsAPI.luckPermsAPI().setDisplayName(LuckPermsAPI.luckPermsAPI().getUser(player.getUuid()));
    }

    private void playerLogin(PlayerLoginEvent playerLoginEvent) {
        Player player = playerLoginEvent.getPlayer();
        LuckPermsAPI.luckPermsAPI().setDisplayName(LuckPermsAPI.luckPermsAPI().getUser(player.getUuid()));
    }

    private void playerChat(PlayerChatEvent playerChatEvent) {
        Player player = playerChatEvent.getPlayer();
        var group = LuckPermsAPI.luckPermsAPI().getPrimaryGroup(player.getUuid());
        String displayName = LuckPermsAPI.luckPermsAPI().getGroupPrefix(group) + player.getUsername();
        playerChatEvent.setChatFormat(playerChatEvent1 -> Component.text()
                .append(MiniMessage.miniMessage().deserialize(displayName))
                .append(Component.text(": "))
                .append(MiniMessage.miniMessage().deserialize(playerChatEvent1.getMessage()))
                .build());
    }

    @Override
    public void terminate() {
        LuckPermsAPI.luckPermsAPI().unsubscribeEvents();
    }
}
