package net.onelitefeather.butterfly;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.meta.CommandMeta;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import cloud.commandframework.paper.PaperCommandManager;

import java.util.function.Function;

public class Butterfly extends JavaPlugin {


    private final PaperCommandManager<CommandSender> paperCommandManager;
    private final AnnotationParser<CommandSender> annotationParser;

    final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
            CommandMeta.simple().with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description")).build();

    public Butterfly(PaperCommandManager<CommandSender> paperCommandManager) {
        this.paperCommandManager = paperCommandManager;
        this.annotationParser = new AnnotationParser<>(paperCommandManager, CommandSender.class, commandMetaFunction);
    }

    @Override
    public void onEnable() {

    }



    @Override
    public void onDisable() {

    }
}
