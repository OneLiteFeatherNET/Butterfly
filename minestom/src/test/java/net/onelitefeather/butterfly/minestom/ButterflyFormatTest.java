package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ButterflyFormatTest {

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static Stream<Component> flatten(Component component) {
        return Stream.concat(Stream.of(component), component.children().stream().flatMap(ButterflyFormatTest::flatten));
    }

    @Test
    @DisplayName("Should keep a message literal when the player may not format chat")
    void testUnformattedMessage() {
        Component message = ButterflyFormat.chatMessage("<red>hello", false);

        assertEquals("<red>hello", plain(message), "The tag has to survive as text");
        assertTrue(flatten(message).allMatch(part -> part.color() == null),
                "An unformatted message must not carry a color");
    }

    @Test
    @DisplayName("Should apply colors of a player that may format chat")
    void testColoredMessage() {
        Component message = ButterflyFormat.chatMessage("<red>hello", true);

        assertEquals("hello", plain(message));
        assertTrue(flatten(message).anyMatch(part -> part.color() == NamedTextColor.RED),
                "The message has to be colored red");
    }

    @Test
    @DisplayName("Should apply decorations of a player that may format chat")
    void testDecoratedMessage() {
        Component message = ButterflyFormat.chatMessage("<bold>hello", true);

        assertEquals("hello", plain(message));
        assertTrue(flatten(message).anyMatch(part -> part.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE),
                "The message has to be bold");
    }

    @Test
    @DisplayName("Should apply gradients of a player that may format chat")
    void testGradientMessage() {
        Component message = ButterflyFormat.chatMessage("<gradient:red:blue>hello", true);

        assertEquals("hello", plain(message));
        assertTrue(flatten(message).anyMatch(part -> part.color() != null), "The message has to be colored");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<click:run_command:'/op notch'>here</click>",
            "<hover:show_text:'secret'>here</hover>",
            "<insertion:hi>here</insertion>",
            "<newline>",
            "<selector:@a>",
            "<score:board:objective>",
            "<nbt:entity:@s:Pos>",
            "<lang:chat.type.text>"
    })
    @DisplayName("Should not let a player build interactive or forged chat content")
    void testRejectedTags(String rawMessage) {
        Component message = ButterflyFormat.chatMessage(rawMessage, true);

        assertEquals(rawMessage, plain(message), "The tag has to stay literal text");
        assertTrue(flatten(message).allMatch(part -> part.clickEvent() == null),
                "A player must not be able to attach a click event");
        assertTrue(flatten(message).allMatch(part -> part.hoverEvent() == null),
                "A player must not be able to attach a hover event");
    }

    @Test
    @DisplayName("Should not let a player forge a second chat line")
    void testNoForgedNewline() {
        Component message = ButterflyFormat.chatMessage("hi<newline>Server: shutting down", true);

        assertTrue(plain(message).indexOf('\n') < 0, "A chat message must stay on one line");
    }

    @Test
    @DisplayName("Should join the display name and the message into one chat line")
    void testChatLine() {
        Component line = ButterflyFormat.chatLine(
                Component.text("[Admin] Notch", NamedTextColor.RED),
                Component.text("hello"));

        assertEquals("[Admin] Notch: hello", plain(line));
    }
}
