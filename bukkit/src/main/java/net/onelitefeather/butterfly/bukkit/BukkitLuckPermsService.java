package net.onelitefeather.butterfly.bukkit;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.api.LuckPermsService;
import net.onelitefeather.butterfly.util.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
            var manager = player.getServer().getScoreboardManager();
            if (player.getScoreboard().equals(manager.getMainScoreboard())) {
                player.setScoreboard(manager.getNewScoreboard());
            }
            var playerScoreboard = player.getScoreboard();
            var weight = group.getWeight().orElse(9999);
            var teamName = String.format("%04d", weight) + group.getName();
            var team = playerScoreboard.getTeam(teamName);
            if (team == null) {
                team = playerScoreboard.registerNewTeam(teamName);
                team.prefix(MiniMessage.miniMessage().deserialize(LuckPermsAPI.luckPermsAPI().getGroupPrefix(group)));
                team.displayName(MiniMessage.miniMessage().deserialize(LuckPermsAPI.luckPermsAPI().getGroupPrefix(group)));
                team.color(getTeamColor(group));
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
