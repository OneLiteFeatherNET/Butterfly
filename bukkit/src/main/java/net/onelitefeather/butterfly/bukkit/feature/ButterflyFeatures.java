package net.onelitefeather.butterfly.bukkit.feature;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;

public enum ButterflyFeatures implements Feature {
    @Label("Team Collision")
    TEAM_COLLISION
    ;

    public boolean isActive() {
        return BukkitFeatureContext.getFeatureManager().isActive(this);
    }
}
