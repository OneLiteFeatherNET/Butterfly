package net.onelitefeather.butterfly.bukkit;

import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.bukkit.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Butterfly extends JavaPlugin {

    @Override
    public void onEnable() {
        LuckPermsAPI.luckPermsAPI().subscribeEvents();
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        LuckPermsAPI.luckPermsAPI().unsubscribeEvents();
    }
}
