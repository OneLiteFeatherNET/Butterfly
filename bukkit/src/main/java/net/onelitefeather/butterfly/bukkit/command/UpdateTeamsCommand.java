package net.onelitefeather.butterfly.bukkit.command;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public class UpdateTeamsCommand extends Command {
    public UpdateTeamsCommand() {
        super("updateteams");
        setPermission("butterfly.updateteams");
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).map(LuckPermsAPI.luckPermsAPI()::getUser).forEach(LuckPermsAPI.luckPermsAPI()::setDisplayName);
        commandSender.sendRichMessage("<green><count> Players have been added to the team", Placeholder.parsed("count", String.valueOf(Bukkit.getOnlinePlayers().size())));
        return true;
    }
}
