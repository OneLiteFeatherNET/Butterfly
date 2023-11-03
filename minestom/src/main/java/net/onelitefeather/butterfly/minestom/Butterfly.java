package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extensions.Extension;
import net.onelitefeather.butterfly.api.LuckPermsAPI;

public class Butterfly extends Extension {
    @Override
    public void initialize() {
        LuckPermsAPI.setLuckPermsService(new MinestomLuckPermsService());
        LuckPermsAPI.luckPermsAPI().subscribeEvents();
        MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent.class, this::playerChat);
        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, this::playerLogin);
    }

    private void playerLogin(PlayerLoginEvent playerLoginEvent) {
        Player player = playerLoginEvent.getPlayer();
        String displayName = LuckPermsAPI.luckPermsAPI().getGroupPrefix(LuckPermsAPI.luckPermsAPI().getPrimaryGroup(player.getUuid())) + player.getUsername();
        player.setDisplayName(MiniMessage.miniMessage().deserialize(displayName));
    }

    private void playerChat(PlayerChatEvent playerChatEvent) {

        playerChatEvent.setChatFormat(playerChatEvent1 -> Component.text()
                .append(playerChatEvent1.getPlayer().getDisplayName())
                .append(Component.text(": "))
                .append(MiniMessage.miniMessage().deserialize(playerChatEvent1.getMessage()))
                .build());
    }

    @Override
    public void terminate() {
        LuckPermsAPI.luckPermsAPI().unsubscribeEvents();
    }
}
