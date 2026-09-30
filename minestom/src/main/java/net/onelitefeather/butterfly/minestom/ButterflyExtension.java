package net.onelitefeather.butterfly.minestom;

import net.minestom.server.extensions.Extension;
import net.onelitefeather.minestom.extensions.processor.ExtensionInfo;

/**
 * Runs Butterfly as a Minestom extension.
 *
 * <p>{@link ExtensionInfo} generates {@code extension.json} at compile time; the version comes from the
 * {@code minestom.extension.version} compiler argument. LuckPerms runs inside the host and is not an
 * extension, so no dependency is declared.
 */
@ExtensionInfo(name = "Butterfly")
public final class ButterflyExtension extends Extension {

    private Butterfly butterfly;

    @Override
    public void initialize() {
        butterfly = Butterfly.create();
        butterfly.load();
    }

    @Override
    public void terminate() {
        if (butterfly != null) {
            butterfly.terminate();
        }
    }
}
