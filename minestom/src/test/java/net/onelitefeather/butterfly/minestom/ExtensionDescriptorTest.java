package net.onelitefeather.butterfly.minestom;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionDescriptorTest {

    private JsonObject descriptor;

    @BeforeEach
    void loadDescriptor() throws IOException {
        try (var stream = ExtensionDescriptorTest.class.getResourceAsStream("/extension.json")) {
            assertNotNull(stream, "extension.json must be generated into the module output");
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                descriptor = new Gson().fromJson(reader, JsonObject.class);
            }
        }
    }

    @Test
    @DisplayName("descriptor is named Butterfly")
    void descriptorHasExtensionName() {
        assertEquals("Butterfly", descriptor.get("name").getAsString());
    }

    @Test
    @DisplayName("descriptor points at the extension entry point")
    void descriptorHasEntrypoint() {
        assertEquals(ButterflyExtension.class.getName(), descriptor.get("entrypoint").getAsString());
    }

    @Test
    @DisplayName("descriptor carries the project version passed by the build")
    void descriptorHasProjectVersion() {
        String expected = System.getProperty("butterfly.expected.version");
        assertNotNull(expected, "build must pass butterfly.expected.version to the test JVM");
        assertEquals(expected, descriptor.get("version").getAsString());
    }

    @Test
    @DisplayName("descriptor declares no LuckPerms dependency")
    void descriptorHasNoLuckPermsDependency() {
        JsonArray dependencies = descriptor.has("dependencies") ? descriptor.getAsJsonArray("dependencies") : new JsonArray();
        dependencies.forEach(dependency ->
                assertTrue(!dependency.getAsString().equalsIgnoreCase("LuckPerms"), "LuckPerms runs in the host, not as an extension"));
        assertEquals(0, dependencies.size(), "Butterfly must not depend on any extension");
    }
}
