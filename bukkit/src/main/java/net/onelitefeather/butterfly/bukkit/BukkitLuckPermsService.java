package net.onelitefeather.butterfly.bukkit;

import com.google.auto.service.AutoService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.api.LuckPermsService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@AutoService(LuckPermsService.class)
public class BukkitLuckPermsService implements LuckPermsService {

    @Override
    public LuckPerms getLuckPerms() {
        var server = Bukkit.getServer();
        if (!server.getPluginManager().isPluginEnabled("LuckPerms")) return null;

        var provider = server.getServicesManager().getRegistration(LuckPerms.class);
        if (provider == null) return null;
        return provider.getProvider();
    }

    @Override
    public Group getDefaultGroup() {
        return this.getLuckPerms().getGroupManager().getGroup("default");
    }

    @Override
    public void setDisplayName(User user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (player != null)
            player.displayName(MiniMessage.miniMessage().deserialize(LuckPermsAPI.luckPermsAPI().getGroupPrefix(LuckPermsAPI.luckPermsAPI().getPrimaryGroup(player.getUniqueId())) + player.getName()));
    }
}
