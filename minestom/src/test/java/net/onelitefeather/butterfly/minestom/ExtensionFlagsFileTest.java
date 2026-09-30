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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
