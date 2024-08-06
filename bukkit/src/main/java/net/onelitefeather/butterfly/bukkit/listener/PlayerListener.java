package net.onelitefeather.butterfly.bukkit.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public final class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void handlePlayerLogin(PlayerLoginEvent event) {
        LuckPermsAPI.luckPermsAPI().setDisplayName(LuckPermsAPI.luckPermsAPI().getUser(event.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handlePlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        LuckPermsAPI.luckPermsAPI().setDisplayName(LuckPermsAPI.luckPermsAPI().getUser(player.getUniqueId()));
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
