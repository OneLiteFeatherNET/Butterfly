package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.testing.TestConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves a Butterfly chat line survives the whole server: a client chat packet goes in and
 * the assertions are made on the chat packet the other client actually receives.
 * <p>
 * This class deliberately holds a single test. The test harness only delivers chat to a
 * client in the first test of a class, so the remaining chat cases assert the component
 * Butterfly hands to the server in {@link ButterflyChatIntegrationTest} instead.
 */
@EnvTest
class ButterflyChatDeliveryTest {

    @Test
    @DisplayName("Should deliver the prefix, the team colored name and the message to the other player")
    void testChatLineReachesTheOtherPlayer(Env env) {
        Instance instance = env.createFlatInstance();
        Player sender = env.createConnection(new GameProfile(UUID.randomUUID(), "Notch"))
                .connect(instance, new Pos(0, 42, 0));
        TestConnection viewerConnection = env.createConnection(new GameProfile(UUID.randomUUID(), "Viewer"));
        viewerConnection.connect(instance, new Pos(0, 42, 0));

        EventNode<Event> node = EventNode.all("butterfly-chat-delivery");
        env.process().eventHandler().addChild(node);
        node.addListener(PlayerChatEvent.class, event -> {
            Component prefix = MiniMessage.miniMessage().deserialize("<dark_red>[Admin] ");
            Component displayName = Component.text()
                    .append(prefix)
                    .append(Component.text(event.getPlayer().getUsername(), NamedTextColor.DARK_RED))
                    .build();
            event.setFormattedMessage(ButterflyFormat.chatLine(displayName,
                    ButterflyFormat.chatMessage(event.getRawMessage(), true)));
        });

        var packets = viewerConnection.trackIncoming(SystemChatPacket.class);
        env.process().packetListener().processClientPacket(
                new ClientChatMessagePacket("<green>hello lobby", 0L, 0L, null, 0, new BitSet(), (byte) 0),
                sender.getPlayerConnection());

        List<SystemChatPacket> received = packets.collect();
        assertEquals(1, received.size(), "The other player has to receive exactly one chat line");

        Component line = received.getFirst().message();
        assertEquals("[Admin] Notch: hello lobby", PlainTextComponentSerializer.plainText().serialize(line));
        assertTrue(flatten(line).anyMatch(part -> part.color() == NamedTextColor.DARK_RED),
                "The prefix and the name have to arrive colored");
        assertTrue(flatten(line).anyMatch(part -> part.color() == NamedTextColor.GREEN),
                "The color the player typed has to arrive");

        env.process().eventHandler().removeChild(node);
    }

    private static Stream<Component> flatten(Component component) {
        return Stream.concat(Stream.of(component),
                component.children().stream().flatMap(ButterflyChatDeliveryTest::flatten));
    }
}
