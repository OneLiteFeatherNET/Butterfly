package net.onelitefeather.butterfly.minestom;

import net.luckperms.api.LuckPermsProvider;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.onelitefeather.butterfly.minestom.feature.ButterflyFeatures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Start/stop logic of the extension, kept apart from {@link ButterflyExtension} so it can run without
 * an extension class loader.
 *
 * <p>{@link #start()} is the first place that touches LuckPerms; it must not run before the host has
 * registered LuckPerms (that is, not in {@code preInitialize()}).
 */
final class ButterflyLifecycle {

    private final Logger logger;
    private final EventNode<Event> parent;
    private final Path dataDirectory;
    private @Nullable Butterfly butterfly;

    ButterflyLifecycle(@NotNull Logger logger, @NotNull EventNode<Event> parent, @NotNull Path dataDirectory) {
        this.logger = logger;
        this.parent = parent;
        this.dataDirectory = dataDirectory;
    }

    void start() {
        try {
            LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            logger.error("LuckPerms is not available, Butterfly stays inactive: {}", e.getMessage(), e);
            return;
        }
        ButterflyFeatures.configure(dataDirectory.resolve("flags.properties"));
        butterfly = Butterfly.create(parent);
        butterfly.load();
    }

    void stop() {
        if (butterfly != null) {
            butterfly.terminate();
            butterfly = null;
            ButterflyFeatures.reset();
        }
    }

    boolean isActive() {
        return butterfly != null;
    }
}
