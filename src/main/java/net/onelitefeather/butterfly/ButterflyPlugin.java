package net.onelitefeather.butterfly;

import net.onelitefeather.butterfly.service.LuckPermsService;
import org.bukkit.plugin.java.JavaPlugin;

public final class ButterflyPlugin extends JavaPlugin {

    private LuckPermsService luckPermsService;

    @Override
    public void onEnable() {
        this.luckPermsService = new LuckPermsService(this);
        this.luckPermsService.subscribeEvents();
    }

    @Override
    public void onDisable() {
        this.luckPermsService.unsubscribeEvents();
    }
}
