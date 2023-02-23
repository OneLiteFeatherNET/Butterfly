package net.onelitefeather.butterfly.tablist;

import net.onelitefeather.butterfly.Butterfly;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;


public class TablistManager {

    private final Butterfly butterfly;

    public TablistManager(Butterfly butterfly) {
        this.butterfly = butterfly;
    }


    public void setAllPlayerTeams() {
        Bukkit.getOnlinePlayers().forEach(this::setTablist);
    }

    public void setTablist(Player player) {

        Scoreboard scoreboard = player.getScoreboard();

        // ---- create here your roles ----

        Team admin = scoreboard.getTeam("operators");

        if (admin == null) {
            admin = scoreboard.registerNewTeam("operators");
        }

        admin.setPrefix(ChatColor.DARK_RED + "ADMIN " + ChatColor.DARK_GRAY + "| ");

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.hasPermission("")) { // Add here you luckPerms permission <--
                admin.addEntry(target.getName());
                continue;
            }

            // ---- Add here more roles ----
        }
    }
}
