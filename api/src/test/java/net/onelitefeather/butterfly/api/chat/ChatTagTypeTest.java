package net.onelitefeather.butterfly.api.chat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatTagTypeTest {

    @Test
    @DisplayName("the tag types are exactly the ones of the spec table, in order")
    void typesMatchSpecTable() {
        List<String> expected = List.of("color", "decoration", "gradient", "rainbow", "transition", "pride", "shadow",
                "font", "reset", "newline", "click", "hover", "insertion", "keybind", "translatable", "selector",
                "score", "nbt", "sprite", "head");

        List<String> actual = Arrays.stream(ChatTagType.values()).map(ChatTagType::type).toList();

        assertEquals(expected, actual, "the enum must list every type of the spec table");
    }

    @Test
    @DisplayName("a tag type's permission node is butterfly.chat.tag. followed by the type")
    void nodeIsPrefixPlusType() {
        assertEquals("butterfly.chat.tag.shadow", ChatTagType.SHADOW.node(), "node of shadow");
        assertEquals("butterfly.chat.tag.head", ChatTagType.HEAD.node(), "node of head");
    }
}
