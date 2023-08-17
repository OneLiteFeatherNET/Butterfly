package net.onelitefeather.butterfly.bukkit;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.api.LuckPermsService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitLuckPermsService implements LuckPermsService {

    @Override
    public Group getDefaultGroup() {
        return LuckPermsProvider.get().getGroupManager().getGroup("default");
    }

    @Override
    public void setDisplayName(User user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (player != null)
            player.displayName(MiniMessage.miniMessage().deserialize(LuckPermsAPI.luckPermsAPI().getGroupPrefix(LuckPermsAPI.luckPermsAPI().getPrimaryGroup(player.getUniqueId())) + player.getName()));
    }
}
