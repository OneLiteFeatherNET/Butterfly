package net.onelitefeather.butterfly.api.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Nullable;

/**
 * Composes a chat line from its parts, identically on every platform.
 */
public final class ChatLine {

    private ChatLine() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * {@code head + space + name + ": " + message}; without a head there is no leading space.
     *
     * @param head    the sender's head, or {@code null} to leave it out
     * @param name    the sender's prefix and name
     * @param message the message part
     */
    public static Component compose(@Nullable Component head, Component name, Component message) {
        TextComponent.Builder line = Component.text();
        if (head != null) {
            line.append(head).append(Component.space());
        }
        return line.append(name).append(Component.text(": ")).append(message).build();
    }
}
