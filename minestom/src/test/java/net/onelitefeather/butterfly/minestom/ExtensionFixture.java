package net.onelitefeather.butterfly.minestom;

import net.luckperms.api.LuckPermsRegistration;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.player.GameProfile;
import net.minestom.testing.Env;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Shared per-test setup: a fake LuckPerms with one group and one user, and helpers to drive players.
 * Every test builds its own instance.
 */
final class ExtensionFixture {

    static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final String PLAYER_NAME = "Alice";

    final Env env;
    final FakeLuckPerms luckPerms = new FakeLuckPerms();
    final RecordingLogger log = new RecordingLogger();

    final Path dataDirectory;

    ExtensionFixture(Env env, Path dataDirectory) {
        this.env = env;
        this.dataDirectory = dataDirectory;
        luckPerms.addGroup("admin", 100, "<red>[Admin] ", "red");
        luckPerms.addUser(PLAYER_ID, "admin");
    }

    void registerLuckPerms() {
        LuckPermsRegistration.register(luckPerms.api());
    }

    void unregisterLuckPerms() {
        LuckPermsRegistration.unregister();
    }

    ButterflyLifecycle newLifecycle() {
        return new ButterflyLifecycle(log.logger(), env.process().eventHandler(), dataDirectory);
    }

    Player spawnPlayer() {
        Instance instance = env.createFlatInstance();
        Player player = env.createConnection(new GameProfile(PLAYER_ID, PLAYER_NAME)).connect(instance, Pos.ZERO);
        env.tick();
        return player;
    }

    PlayerChatEvent chat(Player player, String message) {
        PlayerChatEvent event = new PlayerChatEvent(player, List.of(player), message);
        env.process().eventHandler().call(event);
        return event;
    }
}
