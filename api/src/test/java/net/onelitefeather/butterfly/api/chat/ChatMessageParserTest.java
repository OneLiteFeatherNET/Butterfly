package net.onelitefeather.butterfly.api.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatMessageParserTest {

    private static final Predicate<String> NOTHING = _ -> false;
    private static final Predicate<String> EVERYTHING = _ -> true;

    private static Predicate<String> granting(String... types) {
        Set<String> nodes = new HashSet<>();
        for (String type : types) nodes.add("butterfly.chat.tag." + type);
        return nodes::contains;
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * All text components of the tree whose own content is not empty, in order.
     */
    private static List<TextComponent> leaves(Component component) {
        List<TextComponent> result = new ArrayList<>();
        collect(component, result);
        return result;
    }

    private static void collect(Component component, List<TextComponent> into) {
        if (component instanceof TextComponent text && !text.content().isEmpty()) into.add(text);
        component.children().forEach(child -> collect(child, into));
    }

    private static TextComponent leaf(Component component, String content) {
        return leaves(component).stream()
                .filter(text -> text.content().equals(content))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no component with content '" + content + "' in " + component));
    }

    private static boolean carriesClickEvent(Component component) {
        return component.clickEvent() != null || component.children().stream().anyMatch(ChatMessageParserTest::carriesClickEvent);
    }

    // Requirement: Tags require a permission per tag type

    @Test
    @DisplayName("a permitted colour tag colours the text and is consumed")
    void permittedColourIsApplied() {
        Component result = ChatMessageParser.parse("<red>hello", granting("color"));

        assertEquals("hello", plain(result), "the tag must not appear in the text");
        assertEquals(NamedTextColor.RED, leaf(result, "hello").color(), "the text must be red");
    }

    @Test
    @DisplayName("permission for one type does not grant another type")
    void permissionForOneTypeDoesNotGrantAnother() {
        Component result = ChatMessageParser.parse("<click:run_command:/op me>x</click>", granting("color"));

        assertFalse(carriesClickEvent(result), "no click event may be present");
        assertEquals("<click:run_command:/op me>x</click>", plain(result), "the tag must stay literal");
    }

    @Test
    @DisplayName("a granted click type produces a click event")
    void permittedClickProducesClickEvent() {
        Component result = ChatMessageParser.parse("<click:run_command:/help>x</click>", granting("click"));

        assertEquals("x", plain(result), "the tag must not appear in the text");
        ClickEvent click = leaf(result, "x").clickEvent();
        assertNotNull(click, "a click event must be present");
        assertEquals(ClickEvent.Action.RUN_COMMAND, click.action(), "the click action must be run_command");
    }

    @Test
    @DisplayName("every type granted renders the rainbow effect")
    void everyTypeGrantedRendersRainbow() {
        Component result = ChatMessageParser.parse("<rainbow>hi</rainbow>", EVERYTHING);

        assertEquals("hi", plain(result), "the tag must not appear in the text");
        Set<TextColor> colours = new HashSet<>();
        leaves(result).forEach(text -> colours.add(text.color()));
        assertFalse(colours.contains(null), "every character must be coloured");
        assertTrue(colours.size() > 1, "the characters must differ in colour, got " + colours);
    }

    @Test
    @DisplayName("a granted gradient type renders the gradient")
    void permittedGradientIsApplied() {
        Component result = ChatMessageParser.parse("<gradient:red:blue>hi</gradient>", granting("gradient"));

        assertEquals("hi", plain(result), "the tag must not appear in the text");
        assertEquals(NamedTextColor.RED.value(), leaf(result, "h").color().value(), "the gradient must start red");
        assertEquals(NamedTextColor.BLUE.value(), leaf(result, "i").color().value(), "the gradient must end blue");
    }

    @Test
    @DisplayName("the parser asks for the node named after the tag type")
    void asksForNodeOfTagType() {
        List<String> asked = new ArrayList<>();
        Predicate<String> recording = node -> {
            asked.add(node);
            return false;
        };

        ChatMessageParser.parse("<red>x", recording);

        assertTrue(asked.contains("butterfly.chat.tag.color"), "node butterfly.chat.tag.color must be checked, got " + asked);
    }

    // Requirement: Disallowed tags stay as literal text

    @Test
    @DisplayName("without any permission the message is the typed text")
    void noPermissionsKeepsTextLiteral() {
        String raw = "<red>hello</red> a < b";

        Component result = ChatMessageParser.parse(raw, NOTHING);

        assertEquals(raw, plain(result), "the text must be unchanged");
        assertTrue(leaves(result).stream().allMatch(text -> text.color() == null), "no colour may be applied");
    }

    @Test
    @DisplayName("an allowed tag is interpreted while a disallowed tag in the same message stays literal")
    void mixedAllowedAndDisallowedTags() {
        Component result = ChatMessageParser.parse("<bold>hi</bold> <red>there", granting("decoration"));

        assertEquals("hi <red>there", plain(result), "only the bold tag may be consumed");
        assertEquals(TextDecoration.State.TRUE, leaf(result, "hi").decoration(TextDecoration.BOLD), "hi must be bold");
        assertEquals(TextDecoration.State.NOT_SET, leaf(result, " <red>there").decoration(TextDecoration.BOLD), "the rest must not be bold");
    }

    @Test
    @DisplayName("plain text without tags is returned as typed")
    void plainTextIsUnchanged() {
        assertEquals("just text", plain(ChatMessageParser.parse("just text", NOTHING)), "text must be unchanged");
    }
}
