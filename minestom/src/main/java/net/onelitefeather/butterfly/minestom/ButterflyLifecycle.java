package net.onelitefeather.butterfly.minestom;

import net.luckperms.api.LuckPermsProvider;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

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
    private @Nullable Butterfly butterfly;

    ButterflyLifecycle(@NotNull Logger logger, @NotNull EventNode<Event> parent) {
        this.logger = logger;
        this.parent = parent;
    }

    void start() {
        try {
            LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            logger.error("LuckPerms is not available, Butterfly stays inactive: {}", e.getMessage(), e);
            return;
        }
        butterfly = Butterfly.create(parent);
        butterfly.load();
    }

    void stop() {
        if (butterfly != null) {
            butterfly.terminate();
            butterfly = null;
        }
    }

    boolean isActive() {
        return butterfly != null;
    }
}
