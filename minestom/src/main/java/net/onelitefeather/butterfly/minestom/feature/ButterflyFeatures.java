package net.onelitefeather.butterfly.minestom.feature;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;

import java.nio.file.Path;

public enum ButterflyFeatures implements Feature, ThreadHelper {
    @Label("Team Collision")
    TEAM_COLLISION
    ;

    private static volatile FeatureManager configured;

    /**
     * Sets the feature manager explicitly instead of letting Togglz find it through {@link java.util.ServiceLoader}
     * and the thread context class loader, which does not see an extension's classes. Flags are read from the
     * given file; a missing file means default values.
     */
    public static void configure(Path flagsFile) {
        configured = SingletonFeatureManagerProvider.createManager(flagsFile.toFile());
    }

    /**
     * Sets a feature manager that only knows the default values, for when no flags file can be used.
     */
    public static void configureDefaults() {
        configured = SingletonFeatureManagerProvider.createManager(null);
    }

    /**
     * Drops the explicitly configured feature manager.
     */
    public static void reset() {
        configured = null;
    }

    @Override
    public boolean isActive() {
        FeatureManager manager = configured;
        if (manager != null) {
            return syncThreadForServiceLoader(() -> manager.isActive(this));
        }
        return syncThreadForServiceLoader(() -> FeatureContext.getFeatureManager().isActive(this));
    }
}
