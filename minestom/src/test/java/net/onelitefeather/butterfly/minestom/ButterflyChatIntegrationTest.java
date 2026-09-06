package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.ServerProcess;
import net.minestom.server.color.TeamColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the real chat pipeline of a running Minestom server: a client chat packet goes in,
 * the server dispatches its own {@link PlayerChatEvent}, and the assertions are made on the
 * component Butterfly hands back for the server to send.
 * <p>
 * {@link ButterflyChatDeliveryTest} covers the last hop to a client; the harness only
 * delivers chat in the first test of a class, so that case lives on its own.
 */
@EnvTest
class ButterflyChatIntegrationTest {

    private static final Pos SPAWN = new Pos(0, 42, 0);

    private ServerProcess process;
    private EventNode<Event> node;

    @AfterEach
    void removeListener() {
        if (this.node != null) {
            this.process.eventHandler().removeChild(this.node);
            this.node = null;
        }
    }

    private static Stream<Component> flatten(Component component) {
        return Stream.concat(Stream.of(component),
                component.children().stream().flatMap(ButterflyChatIntegrationTest::flatten));
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * Registers the formatting Butterfly applies, with the LuckPerms lookup replaced by a
     * fixed group so the test needs no permission backend.
     *
     * @return the chat line the server is handed, once a message has been sent
     */
    private AtomicReference<Component> formatChatAs(Env env, String prefix, TeamColor color, boolean allowFormatting) {
        this.process = env.process();
        this.node = EventNode.all("butterfly-chat-test");
        this.process.eventHandler().addChild(this.node);

        AtomicReference<Component> formatted = new AtomicReference<>();
        this.node.addListener(PlayerChatEvent.class, event -> {
            Component rendered = MiniMessage.miniMessage().deserialize(prefix);
            Component displayName = Component.text()
                    .append(rendered)
                    .append(Component.text(event.getPlayer().getUsername(), color.textColor()))
                    .build();
            event.setFormattedMessage(ButterflyFormat.chatLine(displayName,
                    ButterflyFormat.chatMessage(event.getRawMessage(), allowFormatting)));
            formatted.set(event.getFormattedMessage());
        });
        return formatted;
    }

    private Player connect(Env env, Instance instance, String username) {
        return env.createConnection(new GameProfile(UUID.randomUUID(), username)).connect(instance, SPAWN);
    }

    private void chat(Env env, Player player, String message) {
        env.process().packetListener().processClientPacket(
                new ClientChatMessagePacket(message, 0L, 0L, null, 0, new BitSet(), (byte) 0),
                player.getPlayerConnection());
    }

    private Component chatLine(Env env, String prefix, TeamColor color, boolean allowFormatting, String typed) {
        Instance instance = env.createFlatInstance();
        Player sender = connect(env, instance, "Notch");
        connect(env, instance, "Viewer");

        AtomicReference<Component> formatted = formatChatAs(env, prefix, color, allowFormatting);
        chat(env, sender, typed);

        Component line = formatted.get();
        assertNotNull(line, "The server has to dispatch a chat event for the message");
        return line;
    }

    @Test
    @DisplayName("Should put the prefix and the team colored name in front of the message")
    void testPrefixAndTeamColoredName(Env env) {
        Component line = chatLine(env, "<dark_red>[Admin] ", TeamColor.DARK_RED, false, "hello lobby");

        assertEquals("[Admin] Notch: hello lobby", plain(line));
        assertTrue(flatten(line).anyMatch(part -> part.color() == NamedTextColor.DARK_RED),
                "The prefix and the name have to be colored");
    }

    @Test
    @DisplayName("Should keep chat working for a group without a prefix")
    void testGroupWithoutPrefix(Env env) {
        Component line = chatLine(env, "", TeamColor.GRAY, false, "hello lobby");

        assertEquals("Notch: hello lobby", plain(line), "A missing prefix must not drop the chat format");
        assertTrue(flatten(line).anyMatch(part -> part.color() == NamedTextColor.GRAY),
                "The name still has to carry the team color");
    }

    @Test
    @DisplayName("Should apply MiniMessage colors typed by an allowed player")
    void testPlayerColors(Env env) {
        Component line = chatLine(env, "<gray>", TeamColor.GRAY, true, "<green>hello <yellow>lobby");

        assertEquals("Notch: hello lobby", plain(line), "The tags must not survive as text");
        assertTrue(flatten(line).anyMatch(part -> part.color() == NamedTextColor.GREEN),
                "A color the player typed has to be applied");
        assertTrue(flatten(line).anyMatch(part -> part.color() == NamedTextColor.YELLOW),
                "A second color the player typed has to be applied");
    }

    @Test
    @DisplayName("Should leave tags typed by a disallowed player as plain text")
    void testDisallowedPlayerTagsStayLiteral(Env env) {
        Component line = chatLine(env, "<gray>", TeamColor.GRAY, false, "<green>hello");

        assertEquals("Notch: <green>hello", plain(line));
        assertTrue(flatten(line).noneMatch(part -> part.color() == NamedTextColor.GREEN),
                "A player without the permission must not color their message");
    }

    @Test
    @DisplayName("Should never build a click event a player typed")
    void testTypedClickEventIsRefused(Env env) {
        Component line = chatLine(env, "<gray>", TeamColor.GRAY, true,
                "<click:run_command:'/op me'>free rank</click>");

        assertTrue(flatten(line).allMatch(part -> part.clickEvent() == null),
                "A player must not be able to send a runnable command to another player");
        assertTrue(plain(line).contains("<click:"), "The tag has to stay literal text");
    }

    @Test
    @DisplayName("Should keep a forged second chat line on one line")
    void testForgedChatLineIsRefused(Env env) {
        Component line = chatLine(env, "<gray>", TeamColor.GRAY, true, "hi<newline>Server: restarting now");

        assertFalse(plain(line).contains("\n"), "A player must not be able to forge a second chat line");
    }
}
