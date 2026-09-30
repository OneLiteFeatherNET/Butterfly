package net.onelitefeather.butterfly.minestom;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
import net.onelitefeather.minestom.extensions.processor.ExtensionInfo;

/**
 * Runs Butterfly as a Minestom extension.
 *
 * <p>{@link ExtensionInfo} generates {@code extension.json} at compile time; the version comes from the
 * {@code minestom.extension.version} compiler argument. LuckPerms runs inside the host and is not an
 * extension, so no dependency is declared. Because {@code preInitialize()} runs before LuckPerms is up,
 * LuckPerms is only touched from {@link #initialize()}.
 */
@ExtensionInfo(name = "Butterfly")
public final class ButterflyExtension extends Extension {

    private ButterflyLifecycle lifecycle;

    @Override
    public void initialize() {
        lifecycle = new ButterflyLifecycle(getLogger(), MinecraftServer.getGlobalEventHandler(), getDataDirectory());
        lifecycle.start();
    }

    @Override
    public void terminate() {
        if (lifecycle != null) {
            lifecycle.stop();
            lifecycle = null;
        }
    }
}
