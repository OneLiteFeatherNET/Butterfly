package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
class ExtensionTerminationTest {

    private ExtensionFixture fixture;
    private ButterflyLifecycle lifecycle;

    @BeforeEach
    void startExtension(Env env, @TempDir Path dataDirectory) {
        fixture = new ExtensionFixture(env, dataDirectory);
        fixture.registerLuckPerms();
        lifecycle = fixture.newLifecycle();
        lifecycle.start();
    }

    @AfterEach
    void cleanUp() {
        lifecycle.stop();
        fixture.unregisterLuckPerms();
    }

    @Test
    @DisplayName("after terminate a spawning player is not handled")
    void spawnIsUnhandledAfterTerminate() {
        lifecycle.stop();

        Player player = fixture.spawnPlayer();

        assertNull(player.getTeam(), "no team is assigned after terminate");
    }

    @Test
    @DisplayName("after terminate chat is not formatted")
    void chatIsUnhandledAfterTerminate() {
        Player player = fixture.spawnPlayer();
        lifecycle.stop();

        PlayerChatEvent event = fixture.chat(player, "hello");

        assertFalse(PlainTextComponentSerializer.plainText().serialize(event.getFormattedMessage()).contains("[Admin]"),
                "the group prefix must not be added after terminate");
    }

    @Test
    @DisplayName("terminate removes the teams the extension created")
    void terminateRemovesCreatedTeams() {
        fixture.spawnPlayer();
        assertFalse(MinecraftServer.getTeamManager().getTeams().isEmpty(), "precondition: the extension created a team");

        lifecycle.stop();

        assertTrue(MinecraftServer.getTeamManager().getTeams().isEmpty(), "created teams no longer exist");
    }

    @Test
    @DisplayName("terminate closes the LuckPerms event subscriptions")
    void terminateClosesSubscriptions() {
        assertEquals(1, fixture.luckPerms.openSubscriptions(), "precondition: one subscription while active");

        lifecycle.stop();

        assertEquals(0, fixture.luckPerms.openSubscriptions());
    }

    @Test
    @DisplayName("terminate is harmless when called twice")
    void terminateTwiceIsHarmless() {
        lifecycle.stop();

        assertDoesNotThrow(lifecycle::stop);
    }
}
