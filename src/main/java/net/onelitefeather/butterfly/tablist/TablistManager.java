package net.onelitefeather.butterfly.tablist;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.onelitefeather.butterfly.Butterfly;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;


public class TablistManager {

    private final Butterfly butterfly;

    public TablistManager(Butterfly butterfly) {
        this.butterfly = butterfly;
    }


    public void setTablist(Player player) {

        LuckPerms api = butterfly.getApi();
        User user = api.getPlayerAdapter(Player.class).getUser(player);

        System.out.println("SetTablist 28");

        // Add the permission
        if (!player.hasPermission("permission.player")) {
            user.data().add(Node.builder("permission.player").build());
            api.getUserManager().saveUser(user);
            System.out.println("hasPermission check");
        }

        Scoreboard scoreboard = player.getScoreboard();

        Team admins = scoreboard.getTeam("admins");

        if (admins == null) {
            admins = scoreboard.registerNewTeam("admins");
        }

        admins.setPrefix(ChatColor.RED + "[Admin] ");
        admins.setColor(ChatColor.RED);

    }
}
