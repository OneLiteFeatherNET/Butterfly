package net.onelitefeather.butterfly.listeners;

import net.onelitefeather.butterfly.Butterfly;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Butterfly butterfly;

    public PlayerJoinListener(Butterfly butterfly) {
        this.butterfly = butterfly;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        butterfly.getTablistManager().setAllPlayerTeams();
    }
}
