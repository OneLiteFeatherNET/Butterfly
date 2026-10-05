package net.onelitefeather.butterfly.api.chat;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import java.util.function.Supplier;

/**
 * The MiniMessage tag types a chat sender can be allowed to use. Each type maps to one permission node
 * ({@link #node()}) and to the tag resolvers that make its tags work.
 */
public enum ChatTagType {
    COLOR("color", StandardTags::color),
    DECORATION("decoration", StandardTags::decorations),
    GRADIENT("gradient", StandardTags::gradient),
    RAINBOW("rainbow", StandardTags::rainbow),
    TRANSITION("transition", StandardTags::transition),
    PRIDE("pride", StandardTags::pride),
    SHADOW("shadow", StandardTags::shadowColor),
    FONT("font", StandardTags::font),
    RESET("reset", StandardTags::reset),
    NEWLINE("newline", StandardTags::newline),
    CLICK("click", StandardTags::clickEvent),
    HOVER("hover", StandardTags::hoverEvent),
    INSERTION("insertion", StandardTags::insertion),
    KEYBIND("keybind", StandardTags::keybind),
    TRANSLATABLE("translatable", () -> TagResolver.resolver(StandardTags.translatable(), StandardTags.translatableFallback())),
    SELECTOR("selector", StandardTags::selector),
    SCORE("score", StandardTags::score),
    NBT("nbt", StandardTags::nbt),
    SPRITE("sprite", StandardTags::sprite),
    HEAD("head", StandardTags::sequentialHead);

    /**
     * Prefix shared by all tag permission nodes; {@code butterfly.chat.tag.*} grants every type.
     */
    public static final String NODE_PREFIX = "butterfly.chat.tag.";

    private final String type;
    private final TagResolver resolver;

    ChatTagType(String type, Supplier<TagResolver> resolver) {
        this.type = type;
        this.resolver = resolver.get();
    }

    /**
     * @return the lower-case type name used in the permission node
     */
    public String type() {
        return type;
    }

    /**
     * @return the permission node a sender needs to use this tag type
     */
    public String node() {
        return NODE_PREFIX + type;
    }

    /**
     * @return the resolver that interprets the tags of this type
     */
    public TagResolver resolver() {
        return resolver;
    }
}
