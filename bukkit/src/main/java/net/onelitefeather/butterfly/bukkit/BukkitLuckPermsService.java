package net.onelitefeather.butterfly.bukkit;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.api.LuckPermsService;
import net.onelitefeather.butterfly.bukkit.feature.ButterflyFeatures;
import net.onelitefeather.butterfly.util.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class BukkitLuckPermsService implements LuckPermsService {

    private static final List<String> COLOR_NAMES = new ArrayList<>(NamedTextColor.NAMES.keys());

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
            if (team != null && team.hasPlayer(player)) {
                team.removePlayer(player);
            }
            mainScoreboard.getTeams().forEach(allTeams -> {
                allTeams.removePlayer(player);
            });
            if (team == null) {
                team = mainScoreboard.registerNewTeam(teamName);
                team.prefix(MiniMessage.miniMessage().deserialize(LuckPermsAPI.luckPermsAPI().getGroupPrefix(group)));
                team.displayName(MiniMessage.miniMessage().deserialize(LuckPermsAPI.luckPermsAPI().getGroupPrefix(group)));
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                team.color(getTeamColor(group));
            }
            if (ButterflyFeatures.TEAM_COLLISION.isActive()) {
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
            } else {
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            }
            team.addPlayer(player);
        }
    }

    @NotNull
    private NamedTextColor getTeamColor(@NotNull Group group) {

        NamedTextColor namedTextColor = null;
        for (int i = 0; i < COLOR_NAMES.size() && namedTextColor == null; i++) {
            String colorName = COLOR_NAMES.get(i);
            var perm = Constants.TEAM_COLOR_PERMISSION.formatted(group.getWeight().orElse(-1), colorName);
            if (group.getCachedData().getPermissionData().queryPermission(perm).result().asBoolean()) {
                namedTextColor = NamedTextColor.NAMES.value(colorName);
            }
        }

        return namedTextColor != null ? namedTextColor : NamedTextColor.WHITE;
    }
}
