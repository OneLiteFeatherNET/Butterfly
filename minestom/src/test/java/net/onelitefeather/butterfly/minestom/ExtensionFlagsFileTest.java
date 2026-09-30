package net.onelitefeather.butterfly.minestom;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.testing.Env;
import net.onelitefeather.butterfly.minestom.feature.ButterflyFeatures;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
class ExtensionFlagsFileTest {

    private ExtensionFixture fixture;
    private ButterflyLifecycle lifecycle;

    @AfterEach
    void cleanUp() {
        lifecycle.stop();
        fixture.unregisterLuckPerms();
    }

    @Test
    @DisplayName("flags.properties in the data directory switches team collision on")
    void flagsFileEnablesCollision(Env env, @TempDir Path dataDirectory) throws IOException {
        Files.writeString(dataDirectory.resolve("flags.properties"), "TEAM_COLLISION=true\n");
        fixture = new ExtensionFixture(env, dataDirectory);
        fixture.registerLuckPerms();
        lifecycle = fixture.newLifecycle();
        lifecycle.start();

        Player player = fixture.spawnPlayer();

        assertEquals(TeamsPacket.CollisionRule.ALWAYS, player.getTeam().getCollisionRule());
    }

    @Test
    @DisplayName("a missing data directory is created on start")
    void startCreatesDataDirectory(Env env, @TempDir Path root) {
        Path dataDirectory = root.resolve("extensions").resolve("Butterfly");
        fixture = new ExtensionFixture(env, dataDirectory);
        fixture.registerLuckPerms();
        lifecycle = fixture.newLifecycle();
        lifecycle.start();

        assertTrue(Files.isDirectory(dataDirectory), "data directory must exist after start");
    }

    @Test
    @DisplayName("evaluating a flag with a fresh data directory yields the default and creates flags.properties")
    void freshDataDirectoryYieldsDefault(Env env, @TempDir Path root) {
        Path dataDirectory = root.resolve("extensions").resolve("Butterfly");
        fixture = new ExtensionFixture(env, dataDirectory);
        fixture.registerLuckPerms();
        lifecycle = fixture.newLifecycle();
        lifecycle.start();

        boolean active = ButterflyFeatures.TEAM_COLLISION.isActive();

        assertFalse(active, "team collision defaults to off");
        assertTrue(Files.isRegularFile(dataDirectory.resolve("flags.properties")), "flags.properties must be created");
        assertEquals(List.of(), fixture.log.errors(), "no error may be logged");
    }

    @Test
    @DisplayName("an unusable data directory falls back to defaults with a single warning")
    void unusableDataDirectoryFallsBackToDefaults(Env env, @TempDir Path root) throws IOException {
        Path blocker = Files.writeString(root.resolve("blocker"), "not a directory");
        fixture = new ExtensionFixture(env, blocker.resolve("Butterfly"));
        fixture.registerLuckPerms();
        lifecycle = fixture.newLifecycle();
        lifecycle.start();

        assertFalse(ButterflyFeatures.TEAM_COLLISION.isActive(), "team collision defaults to off");
        assertFalse(ButterflyFeatures.TEAM_COLLISION.isActive(), "still off on the second evaluation");
        assertEquals(1, fixture.log.warnings().size(), "exactly one warning");
        assertEquals(List.of(), fixture.log.errors(), "no error may be logged");
        assertTrue(lifecycle.isActive(), "the extension still starts");
    }
}
