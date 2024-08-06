package net.onelitefeather.butterfly.bukkit.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public class ClearTeamsCommand extends Command {
    public ClearTeamsCommand() {
        super("clearteams");
        setPermission("butterfly.clearteams");
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        mainScoreboard.getTeams().forEach(Team::unregister);
        commandSender.sendRichMessage("<red>Teams have been removed");
        return true;
    }
}
