package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.color.TeamColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The extension behaves like the library did: same team naming, colour, prefix and chat format.
 */
@EnvTest
class ExtensionBehaviourTest {

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

    @Test
    @DisplayName("team collision is off when no setting enables it")
    void collisionIsOffByDefault() {
        Player player = fixture.spawnPlayer();

        assertEquals(TeamsPacket.CollisionRule.NEVER, player.getTeam().getCollisionRule());
    }

    @Test
    @DisplayName("team prefix is the highest prefix of any parent group, not the primary group's")
    void teamPrefixUsesEffectivePrefix() {
        fixture.luckPerms.addGroup("content", 85, "[Content] ", null);
        fixture.luckPerms.addUser(ExtensionFixture.PLAYER_ID, "admin", "[Content] ");

        Player player = fixture.spawnPlayer();

        assertEquals("[Content] ", PlainTextComponentSerializer.plainText().serialize(player.getTeam().getPrefix()));
    }

    @Test
    @DisplayName("chat uses the highest prefix of any parent group, not the primary group's")
    void chatUsesEffectivePrefix() {
        fixture.luckPerms.addGroup("content", 85, "[Content] ", null);
        fixture.luckPerms.addUser(ExtensionFixture.PLAYER_ID, "admin", "[Content] ");
        Player player = fixture.spawnPlayer();

        PlayerChatEvent event = fixture.chat(player, "hi");

        assertEquals("[Content] Alice: hi", PlainTextComponentSerializer.plainText().serialize(event.getFormattedMessage()));
    }

    @Test
    @DisplayName("a user-own prefix reported by luckperms is used for chat and display name")
    void userOwnPrefixWins() {
        fixture.luckPerms.addUser(ExtensionFixture.PLAYER_ID, "admin", "[VIP] ");
        Player player = fixture.spawnPlayer();

        PlayerChatEvent event = fixture.chat(player, "hi");

        assertEquals("[VIP] Alice: hi", PlainTextComponentSerializer.plainText().serialize(event.getFormattedMessage()));
        assertEquals("[VIP] Alice", PlainTextComponentSerializer.plainText().serialize(player.getDisplayName()));
    }

    @Test
    @DisplayName("team colour and name still come from the primary group when the prefix comes from elsewhere")
    void colourAndSortingStayOnPrimaryGroup() {
        fixture.luckPerms.addGroup("content", 85, "[Content] ", "blue");
        fixture.luckPerms.addUser(ExtensionFixture.PLAYER_ID, "admin", "[Content] ");

        Player player = fixture.spawnPlayer();

        assertEquals("0001admin", player.getTeam().getTeamName(), "team name must use the primary group");
        assertEquals(TeamColor.RED, player.getTeam().getTeamColor(), "colour must use the primary group");
    }
}
