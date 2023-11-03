package net.onelitefeather.butterfly.api;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

final class DummyLuckPermsService implements LuckPermsService{

    @Override
    public Group getDefaultGroup() {
        return null;
    }

    @Override
    public void setDisplayName(User user) {
        // no-op
    }
}
