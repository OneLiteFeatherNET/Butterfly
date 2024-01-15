package net.onelitefeather.butterfly.bukkit;

import java.util.Optional;
import java.util.ServiceLoader;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.bukkit.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.togglz.core.activation.SystemPropertyActivationStrategy;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManagerBuilder;

public class Butterfly extends JavaPlugin {

    @Override
    public void onEnable() {
        LuckPermsAPI.setLuckPermsService(new BukkitLuckPermsService());
        LuckPermsAPI.luckPermsAPI().subscribeEvents();
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        LuckPermsAPI.luckPermsAPI().unsubscribeEvents();
    }
}
