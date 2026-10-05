package net.onelitefeather.butterfly.minestom;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.testing.Env;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
class ExtensionConfigFileTest {

    private ExtensionFixture fixture;
    private ButterflyLifecycle lifecycle;

    @AfterEach
    void cleanUp() {
        lifecycle.stop();
        fixture.unregisterLuckPerms();
    }

    private void startExtension(Env env, Path dataDirectory) {
        fixture = new ExtensionFixture(env, dataDirectory);
        fixture.registerLuckPerms();
        lifecycle = fixture.newLifecycle();
        lifecycle.start();
    }

    @Test
    @DisplayName("butterfly.teams.collision in config.yaml switches team collision on")
    void configFileEnablesCollision(Env env, @TempDir Path dataDirectory) throws IOException {
        Files.writeString(dataDirectory.resolve("config.yaml"), "butterfly:\n  teams:\n    collision: true\n");
        startExtension(env, dataDirectory);

        Player player = fixture.spawnPlayer();

        assertEquals(TeamsPacket.CollisionRule.ALWAYS, player.getTeam().getCollisionRule());
    }

    @Test
    @DisplayName("butterfly.teams.sort-format in config.yaml names the team")
    void configFileSetsSortFormat(Env env, @TempDir Path dataDirectory) throws IOException {
        Files.writeString(dataDirectory.resolve("config.yaml"), "butterfly:\n  teams:\n    sort-format: \"%02d\"\n");
        startExtension(env, dataDirectory);

        Player player = fixture.spawnPlayer();

        assertEquals("01admin", player.getTeam().getTeamName());
    }

    @Test
    @DisplayName("a legacy flags.properties is not read and yields one warning naming the replacement key")
    void legacyFlagsFileIsReportedNotRead(Env env, @TempDir Path dataDirectory) throws IOException {
        Files.writeString(dataDirectory.resolve("flags.properties"), "TEAM_COLLISION=true\n");
        startExtension(env, dataDirectory);

        Player player = fixture.spawnPlayer();

        assertEquals(1, fixture.log.warnings().size(), "exactly one warning");
        assertTrue(fixture.log.warnings().getFirst().contains("flags.properties"), "warning names the old file");
        assertTrue(fixture.log.warnings().getFirst().contains("butterfly.teams.collision"), "warning names the new key");
        assertEquals(TeamsPacket.CollisionRule.NEVER, player.getTeam().getCollisionRule());
    }

    @Test
    @DisplayName("a missing data directory is created on start")
    void startCreatesDataDirectory(Env env, @TempDir Path root) {
        Path dataDirectory = root.resolve("extensions").resolve("Butterfly");

        startExtension(env, dataDirectory);

        assertTrue(Files.isDirectory(dataDirectory), "data directory must exist after start");
    }

    @Test
    @DisplayName("a fresh data directory gets a config.yaml and no error is logged")
    void freshDataDirectoryGetsConfigFile(Env env, @TempDir Path root) {
        Path dataDirectory = root.resolve("extensions").resolve("Butterfly");

        startExtension(env, dataDirectory);

        assertTrue(Files.isRegularFile(dataDirectory.resolve("config.yaml")), "config.yaml must be created");
        assertEquals(List.of(), fixture.log.errors(), "no error may be logged");
        assertEquals(List.of(), fixture.log.warnings(), "no warning on a clean first start");
    }

    @Test
    @DisplayName("an unusable data directory falls back to defaults with a single warning")
    void unusableDataDirectoryFallsBackToDefaults(Env env, @TempDir Path root) throws IOException {
        Path blocker = Files.writeString(root.resolve("blocker"), "not a directory");

        startExtension(env, blocker.resolve("Butterfly"));
        Player player = fixture.spawnPlayer();

        assertEquals(TeamsPacket.CollisionRule.NEVER, player.getTeam().getCollisionRule(), "team collision defaults to off");
        assertEquals(1, fixture.log.warnings().size(), "exactly one warning");
        assertEquals(List.of(), fixture.log.errors(), "no error may be logged");
        assertTrue(lifecycle.isActive(), "the extension still starts");
    }
}
