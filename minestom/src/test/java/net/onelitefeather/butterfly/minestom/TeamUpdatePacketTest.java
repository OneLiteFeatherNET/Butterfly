package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.color.TeamColor;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Players who are already online receive the prefix and colour of a team as an update packet;
 * {@code Team#setX} alone only changes server state.
 */
@EnvTest
class TeamUpdatePacketTest {

    private static final UUID OBSERVER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private ExtensionFixture fixture;
    private ButterflyLifecycle lifecycle;

    @BeforeEach
    void startExtension(Env env, @TempDir Path dataDirectory) {
        fixture = new ExtensionFixture(env, dataDirectory);
        fixture.luckPerms.addGroup("member", 10, "<gray>[Member] ", "gray");
        fixture.luckPerms.addUser(OBSERVER_ID, "member");
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
    @DisplayName("an online player receives prefix and colour of the team of a player who joins later")
    void onlinePlayerReceivesTeamOfJoiningPlayer() {
        var observer = fixture.spawnConnection(OBSERVER_ID, "Bob");
        Collector<TeamsPacket> received = observer.connection().trackIncoming(TeamsPacket.class);

        fixture.spawnPlayer();

        assertTrue(hasUpdate(received, "0001admin", "[Admin] ", TeamColor.RED),
                "observer must receive an update for team 0001admin with prefix '[Admin] ' and colour RED, got " + received.collect());
    }

    @Test
    @DisplayName("an online player receives the new prefix when the group prefix of an existing team changes")
    void onlinePlayerReceivesChangedPrefix() {
        var observer = fixture.spawnConnection(OBSERVER_ID, "Bob");
        var admin = fixture.spawnPlayer();
        Collector<TeamsPacket> received = observer.connection().trackIncoming(TeamsPacket.class);

        fixture.luckPerms.addGroup("admin", 100, "<red>[Boss] ", "red");
        LuckPermsAPI.luckPermsAPI().setDisplayName(LuckPermsAPI.luckPermsAPI().getUser(admin.getUuid()));

        assertTrue(hasUpdate(received, "0001admin", "[Boss] ", TeamColor.RED),
                "observer must receive an update for team 0001admin with prefix '[Boss] ', got " + received.collect());
    }

    private static boolean hasUpdate(Collector<TeamsPacket> received, String teamName, String prefix, TeamColor color) {
        return received.collect().stream()
                .filter(packet -> packet.teamName().equals(teamName))
                .map(TeamsPacket::action)
                .filter(TeamsPacket.UpdateTeamAction.class::isInstance)
                .map(action -> ((TeamsPacket.UpdateTeamAction) action).settings())
                .anyMatch(settings -> PlainTextComponentSerializer.plainText().serialize(settings.teamPrefix()).equals(prefix)
                        && settings.color() == color);
    }
}
