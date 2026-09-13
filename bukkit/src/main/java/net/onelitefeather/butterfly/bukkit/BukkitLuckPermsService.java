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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class BukkitLuckPermsService implements LuckPermsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BukkitLuckPermsService.class);
    private static final List<String> COLOR_NAMES = new ArrayList<>(NamedTextColor.NAMES.keys());
    private static final String FORMAT = System.getProperty("butterfly.format", "%04d");

    @Override
    public Group getDefaultGroup() {
        return LuckPermsProvider.get().getGroupManager().getGroup("default");
    }

    @Override
    public void setDisplayName(User user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (player != null) {
            var group = LuckPermsAPI.luckPermsAPI().getPrimaryGroup(player.getUniqueId());

            var prefixOptional = LuckPermsAPI.luckPermsAPI().getGroupPrefix(group);
            if (prefixOptional.isEmpty()) return;

            var prefix = prefixOptional.get();

            String displayName = prefix + player.getName();
            player.displayName(MiniMessage.miniMessage().deserialize(displayName));
            player.playerListName(MiniMessage.miniMessage().deserialize(displayName));
            var manager = player.getServer().getScoreboardManager();

            if (!player.getScoreboard().equals(manager.getMainScoreboard())) {
                player.setScoreboard(manager.getMainScoreboard());
            }

            var playerScoreboard = player.getScoreboard();

            var playerTeam = playerScoreboard.getEntryTeam(player.getName());
            if (playerTeam != null) playerTeam.removePlayer(player);

            var sortId = LuckPermsAPI.luckPermsAPI().getGroupSortId(group);
            var teamName = String.format(FORMAT, sortId) + group.getName();
            var team = playerScoreboard.getTeam(teamName);

            if (team == null) {
                team = playerScoreboard.registerNewTeam(teamName);
            }
            team.prefix(MiniMessage.miniMessage().deserialize(prefix));
            team.displayName(MiniMessage.miniMessage().deserialize(prefix));
            var resolved = getTeamColor(group);
            team.color(resolved);
            team.addPlayer(player);
            LOGGER.warn("[butterfly-diag] team={} resolved={} readBack={} player={}",
                    teamName, resolved, team.color(), player.getName());
        }
    }

    @NotNull
    private NamedTextColor getTeamColor(@NotNull Group group) {

        var weight = group.getWeight().orElse(-1);
        LOGGER.warn("[butterfly-diag] group={} weight={} checkOrder={}", group.getName(), weight, COLOR_NAMES);

        NamedTextColor namedTextColor = null;
        for (int i = 0; i < COLOR_NAMES.size(); i++) {
            String colorName = COLOR_NAMES.get(i);
            var perm = Constants.TEAM_COLOR_PERMISSION.formatted(weight, colorName);
            var tristate = group.getCachedData().getPermissionData().queryPermission(perm).result();
            LOGGER.warn("[butterfly-diag] [{}] {} -> {}", i, perm, tristate);
            if (namedTextColor == null && tristate.asBoolean()) {
                namedTextColor = NamedTextColor.NAMES.value(colorName);
            }
        }

        LOGGER.warn("[butterfly-diag] winner={} (fallback=WHITE)", namedTextColor);
        return namedTextColor != null ? namedTextColor : NamedTextColor.WHITE;
    }
}
