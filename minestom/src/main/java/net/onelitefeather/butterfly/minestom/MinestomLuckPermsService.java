package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.api.LuckPermsService;

public class MinestomLuckPermsService implements LuckPermsService {
    @Override
    public Group getDefaultGroup() {
        return LuckPermsProvider.get().getGroupManager().getGroup("default");
    }

    @Override
    public void setDisplayName(User user) {
        Player player = MinecraftServer.getConnectionManager().getPlayer(user.getUniqueId());
        if (player != null)
            player.setDisplayName(MiniMessage.miniMessage().deserialize(LuckPermsAPI.luckPermsAPI().getGroupPrefix(LuckPermsAPI.luckPermsAPI().getPrimaryGroup(player.getUuid())) + player.getName()));

    }
}
