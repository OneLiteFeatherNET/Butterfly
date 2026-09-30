package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.color.TeamColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.scoreboard.Team;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The extension behaves like the library did: same team naming, colour, prefix and chat format.
 */
@EnvTest
class ExtensionBehaviourTest {

    private ExtensionFixture fixture;
    private ButterflyLifecycle lifecycle;

    @BeforeEach
    void startExtension(Env env) {
        fixture = new ExtensionFixture(env);
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
    @DisplayName("spawn puts the player into the team named sort id plus group")
    void spawnAssignsSortedTeam() {
        Player player = fixture.spawnPlayer();

        assertNotNull(player.getTeam());
        assertEquals("0001admin", player.getTeam().getTeamName());
    }

    @Test
    @DisplayName("spawn applies the group colour to the team")
    void spawnAppliesTeamColor() {
        Player player = fixture.spawnPlayer();

        assertEquals(TeamColor.RED, player.getTeam().getTeamColor());
    }

    @Test
    @DisplayName("spawn applies the group prefix to the team")
    void spawnAppliesPrefix() {
        Player player = fixture.spawnPlayer();
        Team team = player.getTeam();

        assertEquals("[Admin] ", PlainTextComponentSerializer.plainText().serialize(team.getPrefix()));
    }

    @Test
    @DisplayName("chat is the group prefix followed by the message")
    void chatIsFormatted() {
        Player player = fixture.spawnPlayer();

        PlayerChatEvent event = fixture.chat(player, "hello");

        assertEquals("[Admin] Alice: hello", PlainTextComponentSerializer.plainText().serialize(event.getFormattedMessage()));
    }
}
