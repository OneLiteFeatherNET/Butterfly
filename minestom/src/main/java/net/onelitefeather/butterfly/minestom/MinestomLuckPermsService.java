package net.onelitefeather.butterfly.minestom;

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

public class MinestomLuckPermsService implements LuckPermsService {
    @Override
    public Group getDefaultGroup() {
        return LuckPermsProvider.get().getGroupManager().getGroup("default");
    }

    @Override
    public void setDisplayName(User user) {
        Player player = MinecraftServer.getConnectionManager().getPlayer(user.getUniqueId());
        if (player != null) {
            player.setDisplayName(MiniMessage.miniMessage().deserialize(LuckPermsAPI.luckPermsAPI().getGroupPrefix(LuckPermsAPI.luckPermsAPI().getPrimaryGroup(player.getUuid())) + player.getName()));
            var group = LuckPermsAPI.luckPermsAPI().getPrimaryGroup(player.getUuid());
            var weight = group.getWeight().orElse(9999);
            var teamName = String.format("%04d", weight) + group.getName();
            String displayName = LuckPermsAPI.luckPermsAPI().getGroupPrefix(group) + player.getUsername();
            Team team;
            if (MinecraftServer.getTeamManager().exists(teamName)) {
                team = MinecraftServer.getTeamManager().getTeam(teamName);
            } else {
                team = MinecraftServer.getTeamManager()
                        .createBuilder(teamName)
                        .prefix(MiniMessage.miniMessage().deserialize(LuckPermsAPI.luckPermsAPI().getGroupPrefix(group)))
                        .teamDisplayName(MiniMessage.miniMessage().deserialize(displayName))
                        .nameTagVisibility(TeamsPacket.NameTagVisibility.ALWAYS)
                        .collisionRule(TeamsPacket.CollisionRule.NEVER)
                        .updateTeamPacket()
                        .build();
            }
            player.setTeam(team);
        }
    }
}
