package net.onelitefeather.butterfly.bukkit.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scoreboard.Team;

public final class PlayerListener implements Listener {
    @EventHandler
    public void handlePlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
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
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        if (!team.hasPlayer(player)) {
            team.removePlayer(player);
        }

        team.addPlayer(player);

    }

    @EventHandler
    public void handleChat(AsyncChatEvent event) {
        event.renderer((source, sourceDisplayName, message, viewer) -> Component.text()
                .append(sourceDisplayName)
                .append(Component.text(": "))
                .append(MiniMessage.miniMessage().deserialize(PlainTextComponentSerializer.plainText().serialize(message)))
                .build());
    }
}
