package net.onelitefeather.butterfly.minestom;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
class ExtensionActivationTest {

    private ButterflyLifecycle lifecycle;
    private ExtensionFixture fixture;

    @AfterEach
    void cleanUp() {
        if (lifecycle != null) lifecycle.stop();
        if (fixture != null) fixture.unregisterLuckPerms();
    }

    @Test
    @DisplayName("without LuckPerms initialize logs an error naming LuckPerms")
    void missingLuckPermsLogsError(Env env, @TempDir Path dataDirectory) {
        fixture = new ExtensionFixture(env, dataDirectory);
        lifecycle = fixture.newLifecycle();

        assertDoesNotThrow(lifecycle::start, "a missing LuckPerms must not escape the lifecycle method");

        assertEquals(1, fixture.log.errors().size(), "exactly one error is logged");
        assertTrue(fixture.log.errors().getFirst().contains("LuckPerms"), "the error names LuckPerms");
    }

    @Test
    @DisplayName("without LuckPerms the extension stays inactive")
    void missingLuckPermsStaysInactive(Env env, @TempDir Path dataDirectory) {
        fixture = new ExtensionFixture(env, dataDirectory);
        lifecycle = fixture.newLifecycle();

        lifecycle.start();

        assertFalse(lifecycle.isActive());
    }

    @Test
    @DisplayName("without LuckPerms no listener handles a spawning player")
    void missingLuckPermsRegistersNoListeners(Env env, @TempDir Path dataDirectory) {
        fixture = new ExtensionFixture(env, dataDirectory);
        lifecycle = fixture.newLifecycle();
        lifecycle.start();

        Player player = fixture.spawnPlayer();

        assertNull(player.getTeam(), "no team is assigned");
        assertTrue(MinecraftServer.getTeamManager().getTeams().isEmpty(), "no team is created");
    }

    @Test
    @DisplayName("with LuckPerms initialize activates the extension without an error")
    void availableLuckPermsActivates(Env env, @TempDir Path dataDirectory) {
        fixture = new ExtensionFixture(env, dataDirectory);
        fixture.registerLuckPerms();
        lifecycle = fixture.newLifecycle();

        lifecycle.start();

        assertTrue(lifecycle.isActive());
        assertEquals(java.util.List.of(), fixture.log.errors());
    }

    @Test
    @DisplayName("with LuckPerms initialize registers the listeners")
    void availableLuckPermsRegistersListeners(Env env, @TempDir Path dataDirectory) {
        fixture = new ExtensionFixture(env, dataDirectory);
        fixture.registerLuckPerms();
        lifecycle = fixture.newLifecycle();
        lifecycle.start();

        Player player = fixture.spawnPlayer();

        assertNotNull(player.getTeam(), "the spawn listener assigned a team");
    }
}
