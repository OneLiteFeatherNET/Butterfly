package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
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

    @Test
    @DisplayName("chat applies a tag the sender has the permission for")
    void chatAppliesPermittedTag() {
        fixture.luckPerms.grantPermissions(ExtensionFixture.PLAYER_ID, "butterfly.chat.tag.color");
        Player player = fixture.spawnPlayer();

        PlayerChatEvent event = fixture.chat(player, "<blue>hello");

        Component message = event.getFormattedMessage();
        assertEquals("[Admin] Alice: hello", PlainTextComponentSerializer.plainText().serialize(message), "the tag must be consumed");
        assertEquals(NamedTextColor.BLUE, textColor(message, "hello"), "the message part must be blue");
    }

    @Test
    @DisplayName("chat keeps a tag literal when the sender lacks the permission")
    void chatKeepsDisallowedTagLiteral() {
        Player player = fixture.spawnPlayer();

        PlayerChatEvent event = fixture.chat(player, "<blue>hello");

        assertEquals("[Admin] Alice: <blue>hello", PlainTextComponentSerializer.plainText().serialize(event.getFormattedMessage()),
                "the tag must stay in the text");
    }

    @Test
    @DisplayName("chat carries no click event without the click permission")
    void chatDropsClickEventWithoutPermission() {
        fixture.luckPerms.grantPermissions(ExtensionFixture.PLAYER_ID, "butterfly.chat.tag.color");
        Player player = fixture.spawnPlayer();

        PlayerChatEvent event = fixture.chat(player, "<click:run_command:/op me>x</click>");

        assertFalse(carriesClickEvent(event.getFormattedMessage()), "no click event may be present");
    }

    @Test
    @DisplayName("the prefix keeps its colour for a sender without tag permissions")
    void prefixKeepsColourWithoutTagPermissions() {
        Player player = fixture.spawnPlayer();

        PlayerChatEvent event = fixture.chat(player, "hi");

        assertEquals(NamedTextColor.RED, textColor(event.getFormattedMessage(), "[Admin] Alice"), "the prefix and name must stay red");
    }

    private static NamedTextColor textColor(Component component, String content) {
        if (component instanceof TextComponent text && text.content().equals(content)) {
            return (NamedTextColor) text.color();
        }
        for (Component child : component.children()) {
            NamedTextColor found = textColor(child, content);
            if (found != null) return found;
        }
        return null;
    }

    private static boolean carriesClickEvent(Component component) {
        return component.clickEvent() != null || component.children().stream().anyMatch(ExtensionBehaviourTest::carriesClickEvent);
    }
}
