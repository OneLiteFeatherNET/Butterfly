package net.onelitefeather.butterfly.minestom;

import java.util.EnumSet;
import java.util.List;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.utils.PacketUtils;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.api.LuckPermsService;
import net.onelitefeather.butterfly.minestom.feature.ButterflyFeatures;
import org.jetbrains.annotations.NotNull;

public class MinestomLuckPermsService implements LuckPermsService {
    @Override
    public Group getDefaultGroup() {
        return LuckPermsProvider.get().getGroupManager().getGroup("default");
    }

    @Override
    public void setDisplayName(User user) {
        Player player = MinecraftServer.getConnectionManager().getPlayer(user.getUniqueId());
        if (player != null) {
            var group = LuckPermsAPI.luckPermsAPI().getPrimaryGroup(player.getUuid());
            var weight = group.getWeight().orElse(9999);
            var teamName = String.format("%04d", weight) + group.getName();
            Team team;
            if (MinecraftServer.getTeamManager().exists(teamName)) {
                team = MinecraftServer.getTeamManager().getTeam(teamName);
            } else {
                team = MinecraftServer.getTeamManager()
                        .createBuilder(teamName)
                        .prefix(MiniMessage.miniMessage().deserialize(LuckPermsAPI.luckPermsAPI().getGroupPrefix(group)))
                        .nameTagVisibility(TeamsPacket.NameTagVisibility.ALWAYS)
                        .updateTeamPacket()
                        .build();
            }
            if (ButterflyFeatures.TEAM_COLLISION.isActive()) {
                team.setCollisionRule(TeamsPacket.CollisionRule.ALWAYS);
            } else {
                team.setCollisionRule(TeamsPacket.CollisionRule.NEVER);
            }
            team.addMember(player.getUsername());
            player.setTeam(team);

            final String displayName = LuckPermsAPI.luckPermsAPI().getGroupPrefix(group) + player.getUsername();
            player.setDisplayName(MiniMessage.miniMessage().deserialize(displayName));
            player.refreshCommands();
            player.triggerStatus((byte)(24 + player.getPermissionLevel()));
        }
    }
}
