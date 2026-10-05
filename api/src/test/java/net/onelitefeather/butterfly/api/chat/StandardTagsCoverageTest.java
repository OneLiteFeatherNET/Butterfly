package net.onelitefeather.butterfly.api.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Confirms, for every row of the chat-tag-permissions spec table, which {@link StandardTags} factory provides the
 * listed tag names: the sample is interpreted when only that factory is enabled and stays literal without it.
 */
class StandardTagsCoverageTest {

    private static final MiniMessage NO_TAGS = MiniMessage.builder().tags(TagResolver.empty()).build();

    static Stream<Arguments> samples() {
        return Stream.of(
                row("color", StandardTags.color(), "<red>x", "<#ff0000>x", "<color:red>x"),
                row("decoration", StandardTags.decorations(), "<bold>x", "<b>x", "<italic>x", "<i>x", "<em>x",
                        "<underlined>x", "<u>x", "<strikethrough>x", "<st>x", "<obfuscated>x", "<obf>x"),
                row("gradient", StandardTags.gradient(), "<gradient:red:blue>x"),
                row("rainbow", StandardTags.rainbow(), "<rainbow>x"),
                row("transition", StandardTags.transition(), "<transition:red:blue:0.5>x"),
                row("pride", StandardTags.pride(), "<pride:gay>x"),
                // Adventure 5.2.0 only knows <shadow> (and <!shadow>); the spec table also lists <shadow_color>, which is not a tag
                row("shadow_color", StandardTags.shadowColor(), "<shadow:red>x"),
                row("font", StandardTags.font(), "<font:uniform>x"),
                row("reset", StandardTags.reset(), "<reset>x"),
                row("newline", StandardTags.newline(), "x<newline>x", "x<br>x"),
                row("click", StandardTags.clickEvent(), "<click:run_command:/help>x</click>"),
                row("hover", StandardTags.hoverEvent(), "<hover:show_text:'hi'>x</hover>"),
                row("insertion", StandardTags.insertion(), "<insert:hi>x"),
                row("keybind", StandardTags.keybind(), "<key:key.jump>"),
                row("translatable", TagResolver.resolver(StandardTags.translatable(), StandardTags.translatableFallback()),
                        "<lang:block.minecraft.diamond_block>", "<tr:block.minecraft.diamond_block>",
                        "<lang_or:block.minecraft.diamond_block:fallback>"),
                row("selector", StandardTags.selector(), "<selector:@p>"),
                row("score", StandardTags.score(), "<score:Steve:objective>"),
                row("nbt", StandardTags.nbt(), "<nbt:entity:@s:Health>"),
                row("sprite", StandardTags.sprite(), "<sprite:blocks:block/stone>"),
                row("head", StandardTags.sequentialHead(), "<head:Notch>")
        ).flatMap(stream -> stream);
    }

    private static Stream<Arguments> row(String type, TagResolver resolver, String... samples) {
        return Stream.of(samples).map(sample -> Arguments.of(type, resolver, sample));
    }

    @ParameterizedTest(name = "{0}: {2}")
    @MethodSource("samples")
    @DisplayName("a tag is interpreted when its factory is enabled")
    void tagIsInterpretedWithItsResolver(String type, TagResolver resolver, String sample) {
        Component literal = NO_TAGS.deserialize(sample);
        Component interpreted = MiniMessage.builder().tags(resolver).build().deserialize(sample);

        assertEquals(Component.text(sample), literal, "without a resolver the sample must stay literal: " + sample);
        assertNotEquals(literal, interpreted, "the " + type + " factory must interpret " + sample);
    }
}
