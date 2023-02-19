package net.onelitefeather.butterfly;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import net.onelitefeather.butterfly.listeners.PlayerJoinListener;
import net.onelitefeather.butterfly.listeners.PlayerLeaveListener;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import cloud.commandframework.paper.PaperCommandManager;

import java.util.function.Function;

public class Butterfly extends JavaPlugin {






    @Override
    public void onEnable() {
        Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
                CommandMeta.simple().with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description")).build();
        PaperCommandManager<CommandSender> paperCommandManager = null;
        try {
            paperCommandManager = new PaperCommandManager<>(this, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        AnnotationParser<CommandSender> annotationParser = new AnnotationParser<>(paperCommandManager, CommandSender.class, commandMetaFunction);




        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(), this);
    }



    @Override
    public void onDisable() {

    }
}
