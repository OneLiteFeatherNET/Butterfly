package net.onelitefeather.butterfly.bukkit;

import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.bukkit.command.ClearTeamsCommand;
import net.onelitefeather.butterfly.bukkit.command.UpdateTeamsCommand;
import net.onelitefeather.butterfly.bukkit.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Butterfly extends JavaPlugin {

    @Override
    public void onEnable() {
        LuckPermsAPI.setLuckPermsService(new BukkitLuckPermsService());
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
