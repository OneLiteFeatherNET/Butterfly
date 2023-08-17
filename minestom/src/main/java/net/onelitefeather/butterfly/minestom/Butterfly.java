package net.onelitefeather.butterfly.minestom;

import net.minestom.server.extensions.Extension;
import net.onelitefeather.butterfly.api.LuckPermsAPI;

public class Butterfly extends Extension {
    @Override
    public void initialize() {
        LuckPermsAPI.setLuckPermsService(new MinestomLuckPermsService());
        LuckPermsAPI.luckPermsAPI().subscribeEvents();
    }

    @Override
    public void terminate() {
        LuckPermsAPI.luckPermsAPI().unsubscribeEvents();
    }
}
