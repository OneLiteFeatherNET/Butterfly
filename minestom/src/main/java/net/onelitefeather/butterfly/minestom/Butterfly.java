package net.onelitefeather.butterfly.minestom;

import net.luckperms.api.model.user.User;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.onelitefeather.butterfly.api.LuckPermsAPI;

import java.util.UUID;

public final class Butterfly {

    public static Butterfly create() {
        return new Butterfly();
    }

    public void load() {
        LuckPermsAPI.setLuckPermsService(new MinestomLuckPermsService());
        LuckPermsAPI.luckPermsAPI().subscribeEvents();

        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(PlayerChatEvent.class, this::playerChat);
        eventHandler.addListener(PlayerSpawnEvent.class, this::playerSpawn);
        eventHandler.addListener(PlayerDisconnectEvent.class, this::playerDisconnect);
    }

    private void playerSpawn(PlayerSpawnEvent playerSpawnEvent) {
        // Fires again on every instance change, where the team is already applied.
        if (!playerSpawnEvent.isFirstSpawn()) return;

        LuckPermsAPI luckPermsAPI = LuckPermsAPI.luckPermsAPI();
        UUID playerUUID = playerSpawnEvent.getPlayer().getUuid();

        User user = luckPermsAPI.getUser(playerUUID);
        if (user != null) {
            luckPermsAPI.setDisplayName(user);
            return;
        }
        // The player reached the instance before LuckPerms finished loading them.
        luckPermsAPI.loadUser(playerUUID).thenAccept(luckPermsAPI::setDisplayName);
    }

    private void playerDisconnect(PlayerDisconnectEvent playerDisconnectEvent) {
        // Teams outlive the players in them, so a leaving player has to be taken out of
        // theirs or the name stays in the team for the rest of the server's lifetime.
        playerDisconnectEvent.getPlayer().setTeam(null);
    }

    private void playerChat(PlayerChatEvent playerChatEvent) {
        Player player = playerChatEvent.getPlayer();
        LuckPermsAPI luckPermsAPI = LuckPermsAPI.luckPermsAPI();

        var format = ButterflyFormat.of(luckPermsAPI.getPrimaryGroup(player.getUuid()), player.getUsername());
        var message = ButterflyFormat.chatMessage(
                playerChatEvent.getRawMessage(), luckPermsAPI.canFormatChat(player.getUuid()));

        playerChatEvent.setFormattedMessage(ButterflyFormat.chatLine(format.displayName(), message));
    }

    public void terminate() {
        LuckPermsAPI.luckPermsAPI().unsubscribeEvents();
    }
}
