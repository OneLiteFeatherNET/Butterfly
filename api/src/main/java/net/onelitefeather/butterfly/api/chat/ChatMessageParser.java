package net.onelitefeather.butterfly.api.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.function.Predicate;

/**
 * Turns the raw text of a chat message into a component, interpreting only the MiniMessage tags the sender may
 * use. Tags that are not allowed stay in the message as literal text.
 */
public final class ChatMessageParser {

    /**
     * Knows no tags by itself; the tags a sender may use are added per call.
     */
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(TagResolver.empty()).build();

    private ChatMessageParser() {
    }

    /**
     * @param raw           the message as the player typed it
     * @param hasPermission checks a permission node of the sender, for example {@code butterfly.chat.tag.color}
     * @return the message with the tags of all permitted {@link ChatTagType}s interpreted
     */
    public static Component parse(String raw, Predicate<String> hasPermission) {
        TagResolver.Builder allowed = TagResolver.builder();
        for (ChatTagType tagType : ChatTagType.values()) {
            if (hasPermission.test(tagType.node())) {
                allowed.resolver(tagType.resolver());
            }
        }
        return MINI_MESSAGE.deserialize(raw, allowed.build());
    }
}
