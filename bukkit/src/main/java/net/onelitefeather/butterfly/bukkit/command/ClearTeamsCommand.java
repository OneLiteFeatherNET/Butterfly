package net.onelitefeather.butterfly.bukkit.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ClearTeamsCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClearTeamsCommand.class);
    private static final Marker SENSITIVITY = MarkerFactory.getMarker("SENSITIVITY");

    public ClearTeamsCommand() {
        super("clearteams");
        setPermission("butterfly.clearteams");
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        mainScoreboard.getTeams().forEach(Team::unregister);
        commandSender.sendRichMessage("<red>Teams have been removed");
        LOGGER.info(SENSITIVITY, "Cleared all teams");
        LOGGER.debug(mainScoreboard.toString());
        return true;
    }
}
