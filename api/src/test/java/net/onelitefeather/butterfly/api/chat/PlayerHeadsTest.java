package net.onelitefeather.butterfly.api.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerHeadsTest {

    private static final UUID ID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");

    private static PlayerHeadObjectContents contentsOf(Component head) {
        ObjectComponent object = assertInstanceOf(ObjectComponent.class, head, "the head is an object component");
        return assertInstanceOf(PlayerHeadObjectContents.class, object.contents(), "the object is a player head");
    }

    @Test
    @DisplayName("the head identifies the player by UUID and name")
    void headCarriesIdAndName() {
        PlayerHeadObjectContents contents = contentsOf(PlayerHeads.of(ID, "Steve", null, null));

        assertEquals(ID, contents.id(), "UUID of the sender");
        assertEquals("Steve", contents.name(), "name of the sender");
    }

    @Test
    @DisplayName("the head carries the textures property with value and signature")
    void headCarriesTextures() {
        PlayerHeadObjectContents contents = contentsOf(PlayerHeads.of(ID, "Steve", "texture-value", "texture-signature"));

        assertEquals(1, contents.profileProperties().size(), "exactly one property");
        PlayerHeadObjectContents.ProfileProperty property = contents.profileProperties().get(0);
        assertEquals("textures", property.name(), "property name");
        assertEquals("texture-value", property.value(), "texture value");
        assertEquals("texture-signature", property.signature(), "texture signature");
    }

    @Test
    @DisplayName("the head carries the textures property without a signature when none is known")
    void headCarriesTexturesWithoutSignature() {
        PlayerHeadObjectContents contents = contentsOf(PlayerHeads.of(ID, "Steve", "texture-value", null));

        assertEquals(1, contents.profileProperties().size(), "exactly one property");
        assertEquals("texture-value", contents.profileProperties().get(0).value(), "texture value");
        assertNull(contents.profileProperties().get(0).signature(), "no signature");
    }

    @Test
    @DisplayName("the head has no property when no texture is known")
    void headWithoutTextureHasNoProperty() {
        PlayerHeadObjectContents contents = contentsOf(PlayerHeads.of(ID, "Steve", null, null));

        assertTrue(contents.profileProperties().isEmpty(), "no profile property without texture");
    }

    @Test
    @DisplayName("a name that is not a valid head name is left out instead of failing")
    void invalidNameIsLeftOut() {
        PlayerHeadObjectContents contents = contentsOf(PlayerHeads.of(ID, "Foo Bar", null, null));

        assertEquals(ID, contents.id(), "UUID still identifies the sender");
        assertNull(contents.name(), "an invalid name is not sent");
    }
}
