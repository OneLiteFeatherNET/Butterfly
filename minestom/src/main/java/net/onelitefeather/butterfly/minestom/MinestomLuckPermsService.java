package net.onelitefeather.butterfly.minestom;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.api.LuckPermsService;
import net.onelitefeather.butterfly.minestom.feature.ButterflyFeatures;
import org.jetbrains.annotations.NotNull;

public class MinestomLuckPermsService implements LuckPermsService {

    private final ButterflyTeams teams = new ButterflyTeams();

    @Override
    public Group getDefaultGroup() {
        return LuckPermsProvider.get().getGroupManager().getGroup("default");
    }

    @Override
    public void setDisplayName(User user) {
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(user.getUniqueId());
        if (player == null) return;

        Group group = LuckPermsAPI.luckPermsAPI().getPrimaryGroup(player.getUuid());
        ButterflyFormat.GroupFormat format = ButterflyFormat.of(group, player.getUsername());

        player.setDisplayName(format.displayName());
        if (format.teamName() != null) {
            // Player#setTeam moves the player out of their previous team for us.
            player.setTeam(this.teams.apply(format.teamName(), format, collisionRule()));
        }

        player.refreshCommands();
        player.triggerStatus((byte) (24 + player.getPermissionLevel()));
    }

    private TeamsPacket.@NotNull CollisionRule collisionRule() {
        return ButterflyFeatures.TEAM_COLLISION.isActive()
                ? TeamsPacket.CollisionRule.ALWAYS
                : TeamsPacket.CollisionRule.NEVER;
    }
}
