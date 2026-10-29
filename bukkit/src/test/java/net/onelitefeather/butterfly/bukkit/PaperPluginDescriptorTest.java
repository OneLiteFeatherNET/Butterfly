package net.onelitefeather.butterfly.bukkit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperPluginDescriptorTest {

    private Map<String, Object> descriptor;

    @BeforeEach
    void loadDescriptor() throws IOException {
        try (var stream = PaperPluginDescriptorTest.class.getResourceAsStream("/paper-plugin.yml")) {
            assertNotNull(stream, "paper-plugin.yml must be generated into the module output");
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                descriptor = new Yaml().load(reader);
            }
        }
    }

    @Test
    @DisplayName("LuckPerms loads before Butterfly so Paper disables Butterfly first")
    void luckPermsLoadsBeforeButterfly() {
        assertEquals("BEFORE", luckPermsEntry().get("load"),
                "Paper disables plugins in reverse load order; LuckPerms must load BEFORE Butterfly or its classes are gone when Butterfly.onDisable runs");
    }

    @Test
    @DisplayName("LuckPerms is a required dependency")
    void luckPermsIsRequired() {
        assertTrue(Boolean.TRUE.equals(luckPermsEntry().get("required")), "LuckPerms must be declared as a required server dependency");
    }

    private Map<String, Object> luckPermsEntry() {
        Map<String, Object> dependencies = child(descriptor, "dependencies");
        Map<String, Object> server = child(dependencies, "server");
        return child(server, "LuckPerms");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> child(Map<String, Object> parent, String key) {
        Object value = parent.get(key);
        assertNotNull(value, "paper-plugin.yml must contain key '" + key + "'");
        return (Map<String, Object>) value;
    }
}
