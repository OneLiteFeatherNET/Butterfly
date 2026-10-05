package net.onelitefeather.butterfly.bukkit.listener;

import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.api.chat.ChatLine;
import net.onelitefeather.butterfly.api.chat.ChatMessageParser;
import net.onelitefeather.butterfly.api.chat.PlayerHeads;
import net.onelitefeather.butterfly.api.config.ButterflySettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

public final class PlayerListener implements Listener {

    private final ButterflySettings settings;

    public PlayerListener(ButterflySettings settings) {
        this.settings = settings;
    }

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
        // Parsed once per message, not once per viewer: only the sender's permissions decide which tags work
        UUID senderId = event.getPlayer().getUniqueId();
        Component parsed = ChatMessageParser.parse(
                PlainTextComponentSerializer.plainText().serialize(event.message()),
                node -> LuckPermsAPI.luckPermsAPI().hasPermission(senderId, node));
        // Built once per message as well; null leaves the head out
        Component head = settings.chatHeadEnabled() ? headOf(event.getPlayer()) : null;
        event.renderer((source, sourceDisplayName, message, viewer) -> ChatLine.compose(head, sourceDisplayName, parsed));
    }

    private static Component headOf(Player player) {
        ProfileProperty textures = player.getPlayerProfile().getProperties().stream()
                .filter(property -> property.getName().equals("textures"))
                .findFirst()
                .orElse(null);
        return PlayerHeads.of(player.getUniqueId(), player.getName(),
                textures == null ? null : textures.getValue(), textures == null ? null : textures.getSignature());
    }
}
