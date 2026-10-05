package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.color.TeamColor;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
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
import java.util.stream.Collectors;

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

        assertEquals(" [Admin] Alice: hello", textWithoutHead(event.getFormattedMessage()));
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

        assertEquals(" [Content] Alice: hi", textWithoutHead(event.getFormattedMessage()));
    }

    @Test
    @DisplayName("a user-own prefix reported by luckperms is used for chat and display name")
    void userOwnPrefixWins() {
        fixture.luckPerms.addUser(ExtensionFixture.PLAYER_ID, "admin", "[VIP] ");
        Player player = fixture.spawnPlayer();

        PlayerChatEvent event = fixture.chat(player, "hi");

        assertEquals(" [VIP] Alice: hi", textWithoutHead(event.getFormattedMessage()));
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
        assertEquals(" [Admin] Alice: hello", textWithoutHead(message), "the tag must be consumed");
        assertEquals(NamedTextColor.BLUE, textColor(message, "hello"), "the message part must be blue");
    }

    @Test
    @DisplayName("chat keeps a tag literal when the sender lacks the permission")
    void chatKeepsDisallowedTagLiteral() {
        Player player = fixture.spawnPlayer();

        PlayerChatEvent event = fixture.chat(player, "<blue>hello");

        assertEquals(" [Admin] Alice: <blue>hello", textWithoutHead(event.getFormattedMessage()),
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

    @Test
    @DisplayName("chat starts with the sender's player head")
    void chatStartsWithPlayerHead() {
        Player player = fixture.spawnPlayer();

        PlayerChatEvent event = fixture.chat(player, "hello");

        PlayerHeadObjectContents head = headOf(event.getFormattedMessage());
        assertNotNull(head, "the first part of the line is a player head");
        assertEquals(ExtensionFixture.PLAYER_ID, head.id(), "the head belongs to the sender");
        assertEquals(ExtensionFixture.PLAYER_NAME, head.name(), "the head carries the sender's name");
    }

    @Test
    @DisplayName("a single space separates the head from the prefix")
    void spaceSeparatesHeadFromPrefix() {
        Player player = fixture.spawnPlayer();

        PlayerChatEvent event = fixture.chat(player, "hello");

        assertEquals(Component.space(), event.getFormattedMessage().children().get(1), "space right after the head");
    }

    @Test
    @DisplayName("the head carries the sender's skin texture and signature")
    void headCarriesSkin() {
        Player player = fixture.spawnPlayer();
        player.setSkin(new PlayerSkin("texture-value", "texture-signature"));

        PlayerChatEvent event = fixture.chat(player, "hello");

        PlayerHeadObjectContents head = headOf(event.getFormattedMessage());
        assertNotNull(head, "the first part of the line is a player head");
        assertEquals(1, head.profileProperties().size(), "exactly one profile property");
        assertEquals("textures", head.profileProperties().get(0).name(), "property name");
        assertEquals("texture-value", head.profileProperties().get(0).value(), "texture value");
        assertEquals("texture-signature", head.profileProperties().get(0).signature(), "texture signature");
    }

    @Test
    @DisplayName("a player without a skin still gets a head with UUID and name but no texture")
    void headWithoutSkinHasNoTexture() {
        Player player = fixture.spawnPlayer();
        player.setSkin(null);

        PlayerChatEvent event = fixture.chat(player, "hello");

        PlayerHeadObjectContents head = headOf(event.getFormattedMessage());
        assertNotNull(head, "the first part of the line is a player head");
        assertEquals(ExtensionFixture.PLAYER_ID, head.id(), "the head belongs to the sender");
        assertTrue(head.profileProperties().isEmpty(), "no texture property without a skin");
    }

    @Test
    @DisplayName("the display name carries no player head")
    void displayNameHasNoHead() {
        Player player = fixture.spawnPlayer();

        fixture.chat(player, "hello");

        assertFalse(containsObject(player.getDisplayName()), "the display name must stay head-free");
        assertEquals("[Admin] Alice", PlainTextComponentSerializer.plainText().serialize(player.getDisplayName()));
    }

    @Test
    @DisplayName("the team prefix carries no player head")
    void teamPrefixHasNoHead() {
        Player player = fixture.spawnPlayer();

        assertFalse(containsObject(player.getTeam().getPrefix()), "the team prefix must stay head-free");
    }

    /** The plain text of the line without the head glyph, so the separating space stays visible. */
    static String textWithoutHead(Component line) {
        return line.children().stream()
                .filter(part -> !(part instanceof ObjectComponent))
                .map(part -> PlainTextComponentSerializer.plainText().serialize(part))
                .collect(Collectors.joining());
    }

    /** The head contents when the first part of the line is a player head, otherwise {@code null}. */
    static PlayerHeadObjectContents headOf(Component line) {
        if (line.children().isEmpty()) return null;
        if (line.children().get(0) instanceof ObjectComponent object && object.contents() instanceof PlayerHeadObjectContents head) {
            return head;
        }
        return null;
    }

    static boolean containsObject(Component component) {
        return component instanceof ObjectComponent || component.children().stream().anyMatch(ExtensionBehaviourTest::containsObject);
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
