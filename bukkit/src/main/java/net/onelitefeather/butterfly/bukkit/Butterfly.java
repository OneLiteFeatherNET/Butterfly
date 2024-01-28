package net.onelitefeather.butterfly.bukkit;

import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.bukkit.feature.BukkitDefaultActivationStrategyProvider;
import net.onelitefeather.butterfly.bukkit.feature.ButterflyFeatures;
import net.onelitefeather.butterfly.bukkit.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.togglz.core.activation.SystemPropertyActivationStrategy;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManagerBuilder;

public class Butterfly extends JavaPlugin {

    private static Plugin INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Thread.currentThread().setContextClassLoader(this.getClassLoader());
        StaticFeatureManagerProvider.setFeatureManager(FeatureManagerBuilder.begin()
                .activationStrategyProvider(new BukkitDefaultActivationStrategyProvider(this.getClassLoader()))
                .activationStrategy(new SystemPropertyActivationStrategy())
                .featureEnum(ButterflyFeatures.class).build());
        LuckPermsAPI.setLuckPermsService(new BukkitLuckPermsService());
        LuckPermsAPI.luckPermsAPI().subscribeEvents();
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        LuckPermsAPI.luckPermsAPI().unsubscribeEvents();
    }

    public static synchronized Plugin getInstance() {
        if (INSTANCE == null) {
            INSTANCE = Bukkit.getPluginManager().getPlugin("Butterfly");
        }
        if (!(INSTANCE instanceof Butterfly)) {
            INSTANCE = Bukkit.getPluginManager().getPlugin("Butterfly");
        }
        return INSTANCE;
    }
}
