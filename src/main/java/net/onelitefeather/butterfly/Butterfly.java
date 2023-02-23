package net.onelitefeather.butterfly;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import net.luckperms.api.LuckPerms;
import net.onelitefeather.butterfly.listeners.PlayerJoinListener;
import net.onelitefeather.butterfly.listeners.PlayerLeaveListener;
import net.onelitefeather.butterfly.tablist.TablistManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Function;

public class Butterfly extends JavaPlugin {

    RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    LuckPerms api;
    private TablistManager tablistManager;

    @Override
    public void onEnable() {

        if(provider != null){
            api = provider.getProvider();
        }

        tablistManager = new TablistManager(this);

        Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
                CommandMeta.simple().with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description")).build();
        PaperCommandManager<CommandSender> paperCommandManager = null;
        try {
            paperCommandManager = new PaperCommandManager<>(this, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        AnnotationParser<CommandSender> annotationParser = new AnnotationParser<>(paperCommandManager, CommandSender.class, commandMetaFunction);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(), this);
    }


    @Override
    public void onDisable() {

    }

    public TablistManager getTablistManager() {
        return tablistManager;
    }

    public LuckPerms getApi() {
        System.out.println("returned API");
        return this.api;
    }
}
