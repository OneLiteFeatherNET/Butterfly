package net.onelitefeather.butterfly.tablist;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
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

        LuckPerms api = butterfly.getApi();

        Scoreboard scoreboard = player.getScoreboard();

        for (Player target : Bukkit.getOnlinePlayers()) {
            String prefix = api.getUserManager().getUser(target.getUniqueId()).getCachedData().getMetaData().getPrefix();

            if(prefix != null){
                System.out.println("Test");

                Team prefixTeam = scoreboard.getTeam(prefix);

                if (prefixTeam == null) {
                    prefixTeam = scoreboard.registerNewTeam(prefix);
                }

                prefixTeam.setPrefix(prefix);

                prefixTeam.addEntry(target.getName());
            }
        }
    }
}
