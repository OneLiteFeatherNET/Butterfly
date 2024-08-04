package net.onelitefeather.butterfly.minestom.feature;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum ButterflyFeatures implements Feature, ThreadHelper {
    @Label("Team Collision")
    TEAM_COLLISION
    ;

    public boolean isActive() {
        return syncThreadForServiceLoader(() -> FeatureContext.getFeatureManager().isActive(this));
    }
}
