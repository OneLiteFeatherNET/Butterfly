package net.onelitefeather.butterfly.api.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Builds the inline player head that precedes a chat line.
 */
public final class PlayerHeads {

    private static final String TEXTURES_PROPERTY = "textures";

    private PlayerHeads() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * The head of the given player. It always carries the UUID and the name; the skin texture is added when
     * known so that clients do not have to look the skin up themselves.
     *
     * @param id        UUID of the player
     * @param name      user name of the player; left out when it is not a valid head name
     * @param texture   value of the {@code textures} profile property, or {@code null} when unknown
     * @param signature signature of the texture, or {@code null}
     */
    public static Component of(UUID id, String name, @Nullable String texture, @Nullable String signature) {
        PlayerHeadObjectContents.Builder builder = ObjectContents.playerHead().id(id);
        // names such as Bedrock ones with spaces are rejected by Adventure; the UUID still identifies the player
        if (PlayerHeadObjectContents.isValidName(name)) {
            builder.name(name);
        }
        if (texture != null) {
            builder.profileProperty(PlayerHeadObjectContents.property(TEXTURES_PROPERTY, texture, signature));
        }
        return Component.object(builder.build());
    }
}
