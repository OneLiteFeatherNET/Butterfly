package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.TeamColor;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.api.LuckPermsService;
import net.onelitefeather.butterfly.api.config.ButterflySettings;
import net.onelitefeather.butterfly.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MinestomLuckPermsService implements LuckPermsService {

    private static final List<String> COLOR_NAMES = new ArrayList<>(NamedTextColor.NAMES.keys());
    private final Set<Team> createdTeams = ConcurrentHashMap.newKeySet();

    private final ButterflySettings settings;

    public MinestomLuckPermsService(@NotNull ButterflySettings settings) {
        this.settings = settings;
    }

    @Override
    public Group getDefaultGroup() {
        return LuckPermsProvider.get().getGroupManager().getGroup("default");
    }

    @Override
    public void setDisplayName(User user) {
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(user.getUniqueId());
        if (player != null) {
            var group = LuckPermsAPI.luckPermsAPI().getPrimaryGroup(player.getUuid());
            var sortId = LuckPermsAPI.luckPermsAPI().getGroupSortId(group);
            var teamName = String.format(settings.sortFormat(), sortId) + group.getName();

            var prefixOptional = LuckPermsAPI.luckPermsAPI().getPlayerPrefix(player.getUuid());
            if(prefixOptional.isEmpty()) return;
            var prefix = prefixOptional.get();

            Team team;
            if (MinecraftServer.getTeamManager().exists(teamName)) {
                team = MinecraftServer.getTeamManager().getTeam(teamName);
            } else {
                team = MinecraftServer.getTeamManager()
                        .createBuilder(teamName)
                        .nameTagVisibility(TeamsPacket.NameTagVisibility.ALWAYS)
                        .updateTeamPacket()
                        .build();
                createdTeams.add(team);
            }
            if (settings.teamCollision()) {
                team.setCollisionRule(TeamsPacket.CollisionRule.ALWAYS);
            } else {
                team.setCollisionRule(TeamsPacket.CollisionRule.NEVER);
            }

            team.setPrefix(MiniMessage.miniMessage().deserialize(prefix));
            team.setTeamColor(getTeamColor(group));
            // set* only changes server state; one update packet delivers all of it to online players
            team.sendUpdatePacket();
            team.addMember(player.getUsername());
            player.setTeam(team);

            final String displayName = prefix + player.getUsername();
            player.setDisplayName(MiniMessage.miniMessage().deserialize(displayName));
            player.refreshCommands();
            player.triggerStatus((byte)(24 + player.getPermissionLevel()));
        }
    }

    /**
     * Deletes the teams this service created and detaches their members.
     */
    void removeCreatedTeams() {
        for (Team team : createdTeams) {
            MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
                    .filter(player -> team.equals(player.getTeam()))
                    .forEach(player -> player.setTeam(null));
            MinecraftServer.getTeamManager().deleteTeam(team);
        }
        createdTeams.clear();
    }

    @NotNull
    private TeamColor getTeamColor(@NotNull Group group) {

        TeamColor teamColor = null;
        for (int i = 0; i < COLOR_NAMES.size() && teamColor == null; i++) {
            String colorName = COLOR_NAMES.get(i);
            var perm = Constants.TEAM_COLOR_PERMISSION.formatted(group.getWeight().orElse(-1), colorName);
            if (group.getCachedData().getPermissionData().queryPermission(perm).result().asBoolean()) {
                teamColor = TeamColor.fromName(colorName);
            }
        }

        return teamColor != null ? teamColor : TeamColor.WHITE;
    }
}
