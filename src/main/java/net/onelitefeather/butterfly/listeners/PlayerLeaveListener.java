package net.onelitefeather.butterfly.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {
    @EventHandler
    public void playerLeave(PlayerQuitEvent event) {
        event.quitMessage(null);
    }
}
