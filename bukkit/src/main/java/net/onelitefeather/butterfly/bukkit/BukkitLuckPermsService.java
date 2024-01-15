package net.onelitefeather.butterfly.bukkit;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.api.LuckPermsService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public final class BukkitLuckPermsService implements LuckPermsService {

    @Override
    public Group getDefaultGroup() {
        return LuckPermsProvider.get().getGroupManager().getGroup("default");
    }

    @Override
    public void setDisplayName(User user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (player != null) {
            var group = LuckPermsAPI.luckPermsAPI().getPrimaryGroup(player.getUniqueId());
            String displayName = LuckPermsAPI.luckPermsAPI().getGroupPrefix(group) + player.getName();
            player.displayName(MiniMessage.miniMessage().deserialize(displayName));
            player.playerListName(MiniMessage.miniMessage().deserialize(displayName));
            var mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            var weight = group.getWeight().orElse(9999);
            var teamName = String.format("%04d", weight) + group.getName();
            var team = mainScoreboard.getTeam(teamName);
            if (team == null) {
                team = mainScoreboard.registerNewTeam(teamName);
                team.prefix(MiniMessage.miniMessage().deserialize(LuckPermsAPI.luckPermsAPI().getGroupPrefix(group)));
                team.displayName(MiniMessage.miniMessage().deserialize(LuckPermsAPI.luckPermsAPI().getGroupPrefix(group)));
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
            }
            if (team.hasPlayer(player)) {
                team.removePlayer(player);
            }

            team.addPlayer(player);
        }
    }
}
