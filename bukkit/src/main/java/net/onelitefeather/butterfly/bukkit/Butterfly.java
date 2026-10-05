package net.onelitefeather.butterfly.bukkit;

import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.api.config.ButterflySettings;
import net.onelitefeather.butterfly.api.config.SettingsFile;
import net.onelitefeather.butterfly.bukkit.command.ClearTeamsCommand;
import net.onelitefeather.butterfly.bukkit.command.UpdateTeamsCommand;
import net.onelitefeather.butterfly.bukkit.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class Butterfly extends JavaPlugin {

    @Override
    public void onEnable() {
        ButterflySettings settings = SettingsFile.load(getDataPath(), "config.yaml", Path.of("flags.properties"), getSLF4JLogger());
        LuckPermsAPI.setLuckPermsService(new BukkitLuckPermsService(settings));
        LuckPermsAPI.luckPermsAPI().subscribeEvents();
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getCommandMap().register("clearteams", new ClearTeamsCommand());
        getServer().getCommandMap().register("updateteams", new UpdateTeamsCommand());
    }

    @Override
    public void onDisable() {
        LuckPermsAPI.luckPermsAPI().unsubscribeEvents();
    }

}
