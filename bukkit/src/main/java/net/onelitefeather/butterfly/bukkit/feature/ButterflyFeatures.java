package net.onelitefeather.butterfly.bukkit.feature;

import net.onelitefeather.butterfly.bukkit.utils.ThreadHelper;
import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum ButterflyFeatures implements Feature, ThreadHelper {
    @EnabledByDefault
    @Label("Team Collision")
    TEAM_COLLISION
    ;

    public boolean isActive() {
        return syncThreadForServiceLoader(() -> FeatureContext.getFeatureManager().isActive(this));
    }
}
