package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.api.LuckPermsService;
import net.onelitefeather.butterfly.minestom.feature.ButterflyFeatures;
import net.onelitefeather.butterfly.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MinestomLuckPermsService implements LuckPermsService {

    private static final List<String> COLOR_NAMES = new ArrayList<>(NamedTextColor.NAMES.keys());

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
            var teamName = String.format("%04d", sortId) + group.getName();
            Team team;
            if (MinecraftServer.getTeamManager().exists(teamName)) {
                team = MinecraftServer.getTeamManager().getTeam(teamName);
            } else {
                team = MinecraftServer.getTeamManager()
                        .createBuilder(teamName)
                        .nameTagVisibility(TeamsPacket.NameTagVisibility.ALWAYS)
                        .updateTeamPacket()
                        .build();
            }
            if (ButterflyFeatures.TEAM_COLLISION.isActive()) {
                team.setCollisionRule(TeamsPacket.CollisionRule.ALWAYS);
            } else {
                team.setCollisionRule(TeamsPacket.CollisionRule.NEVER);
            }

            team.setPrefix(MiniMessage.miniMessage().deserialize(LuckPermsAPI.luckPermsAPI().getGroupPrefix(group)));
            team.setTeamColor(getTeamColor(group));
            team.addMember(player.getUsername());
            player.setTeam(team);

            final String displayName = LuckPermsAPI.luckPermsAPI().getGroupPrefix(group) + player.getUsername();
            player.setDisplayName(MiniMessage.miniMessage().deserialize(displayName));
            player.refreshCommands();
            player.triggerStatus((byte)(24 + player.getPermissionLevel()));
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
