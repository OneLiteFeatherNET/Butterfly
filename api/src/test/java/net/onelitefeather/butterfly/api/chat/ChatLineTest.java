package net.onelitefeather.butterfly.api.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ChatLineTest {

    private static final Component HEAD = PlayerHeads.of(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"), "Steve", null, null);

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    @DisplayName("the line starts with the head")
    void lineStartsWithHead() {
        Component line = ChatLine.compose(HEAD, Component.text("[Admin] Steve"), Component.text("hello"));

        assertSame(HEAD, line.children().get(0), "the head is the first part of the line");
    }

    @Test
    @DisplayName("a single space separates the head from the name")
    void spaceSeparatesHeadFromName() {
        Component line = ChatLine.compose(HEAD, Component.text("[Admin] Steve"), Component.text("hello"));

        assertEquals(Component.space(), line.children().get(1), "space right after the head");
    }

    @Test
    @DisplayName("the line is name, colon, space and message after the head")
    void lineHasNameColonMessage() {
        Component line = ChatLine.compose(HEAD, Component.text("[Admin] Steve"), Component.text("hello"));

        assertEquals(List.of(HEAD, Component.space(), Component.text("[Admin] Steve"), Component.text(": "), Component.text("hello")),
                line.children(), "head, space, name, separator, message");
    }

    @Test
    @DisplayName("without a head the line has no leading space")
    void noHeadNoLeadingSpace() {
        Component line = ChatLine.compose(null, Component.text("[Admin] Steve"), Component.text("hello"));

        assertEquals("[Admin] Steve: hello", plain(line), "plain text without head and leading space");
        assertEquals(List.of(Component.text("[Admin] Steve"), Component.text(": "), Component.text("hello")),
                line.children(), "name, separator, message only");
    }
}
