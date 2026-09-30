package net.luckperms.api;

/**
 * Test-only access to the package-private registration hooks of {@link LuckPermsProvider}.
 */
public final class LuckPermsRegistration {

    private LuckPermsRegistration() {
    }

    public static void register(LuckPerms luckPerms) {
        LuckPermsProvider.register(luckPerms);
    }

    public static void unregister() {
        LuckPermsProvider.unregister();
    }
}
