package net.onelitefeather.butterfly.api;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

public interface LuckPermsService {
    Group getDefaultGroup();

    void setDisplayName(User user);

}
