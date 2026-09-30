package net.onelitefeather.butterfly.minestom;

import net.onelitefeather.butterfly.minestom.feature.ButterflyFeatures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.togglz.core.context.FeatureContext;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Togglz must work inside an extension class loader, where the thread context class loader is the host's.
 * Each test loads Butterfly through its own isolated loader, so nothing is shared between tests.
 */
class FeatureFlagsTest {

    @TempDir
    Path dataDirectory;

    private IsolatedClassLoader newExtensionLoader() {
        URL[] urls = {
                IsolatedClassLoader.locationOf(ButterflyFeatures.class),
                IsolatedClassLoader.locationOf(FeatureContext.class)
        };
        return new IsolatedClassLoader(urls, getClass().getClassLoader());
    }

    /**
     * Configures and queries TEAM_COLLISION inside the isolated loader while the thread context class
     * loader is one that cannot see any Butterfly or Togglz class.
     */
    private boolean teamCollisionUnderForeignContextLoader(Path flagsFile) throws Exception {
        try (var loader = newExtensionLoader()) {
            Class<?> features = Class.forName(ButterflyFeatures.class.getName(), true, loader);
            assertEquals(loader, features.getClassLoader(), "features must come from the isolated loader");
            Object teamCollision = features.getEnumConstants()[0];

            Thread thread = Thread.currentThread();
            ClassLoader original = thread.getContextClassLoader();
            ClassLoader foreign = new ClassLoader(null) {
            };
            thread.setContextClassLoader(foreign);
            try {
                features.getMethod("configure", Path.class).invoke(null, flagsFile);
                boolean active = (boolean) features.getMethod("isActive").invoke(teamCollision);
                assertSame(foreign, thread.getContextClassLoader(), "the caller's context class loader must be restored");
                return active;
            } finally {
                thread.setContextClassLoader(original);
            }
        }
    }

    @Test
    @DisplayName("feature manager is found under a foreign context class loader")
    void featureManagerIsFoundUnderForeignContextLoader() throws Exception {
        Path flags = dataDirectory.resolve("flags.properties");
        Files.writeString(flags, "TEAM_COLLISION=true\n");

        assertTrue(teamCollisionUnderForeignContextLoader(flags));
    }

    @Test
    @DisplayName("flags come from the given data directory file")
    void flagsAreReadFromFile() throws Exception {
        Path flags = dataDirectory.resolve("flags.properties");
        Files.writeString(flags, "TEAM_COLLISION=false\n");

        assertFalse(teamCollisionUnderForeignContextLoader(flags));
    }

    @Test
    @DisplayName("a missing flags file falls back to defaults")
    void missingFlagsFileUsesDefaults() throws Exception {
        Path flags = dataDirectory.resolve("does-not-exist").resolve("flags.properties");

        assertFalse(teamCollisionUnderForeignContextLoader(flags));
    }

    @Test
    @DisplayName("plain Togglz lookup does not find the provider under a foreign context class loader")
    void plainTogglzLookupFailsUnderForeignContextLoader() throws Exception {
        try (var loader = newExtensionLoader()) {
            Class<?> context = Class.forName(FeatureContext.class.getName(), true, loader);
            Thread thread = Thread.currentThread();
            ClassLoader original = thread.getContextClassLoader();
            thread.setContextClassLoader(new ClassLoader(null) {
            });
            try {
                var failure = assertThrows(java.lang.reflect.InvocationTargetException.class,
                        () -> context.getMethod("getFeatureManager").invoke(null));
                assertInstanceOf(IllegalStateException.class, failure.getCause());
            } finally {
                thread.setContextClassLoader(original);
            }
        }
    }
}
