package net.onelitefeather.butterfly.api.config;

import io.avaje.config.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins down the avaje-config builder behaviour Butterfly relies on: exactly one explicit file is read and
 * neither the classpath nor any other directory is scanned.
 */
class AvajeConfigurationSpikeTest {

    @Test
    @DisplayName("a builder without includeResourceLoading ignores application.yaml on the classpath")
    void classpathApplicationYamlIsIgnored() {
        assertNotNull(getClass().getClassLoader().getResource("application.yaml"), "the decoy must be on the classpath");

        Configuration configuration = Configuration.builder().build();

        assertEquals("unset", configuration.get("butterfly.teams.sort-format", "unset"));
    }

    @Test
    @DisplayName("load(File) reads the given file and not an application.yaml next to it")
    void onlyTheGivenFileIsRead(@TempDir Path dataFolder) throws IOException {
        Files.writeString(dataFolder.resolve("application.yaml"), "butterfly:\n  teams:\n    sort-format: \"%decoy\"\n");
        Path config = Files.writeString(dataFolder.resolve("config.yaml"), "butterfly:\n  teams:\n    sort-format: \"%03d\"\n    collision: true\n");

        Configuration configuration = Configuration.builder().load(config.toFile()).build();

        assertEquals("%03d", configuration.get("butterfly.teams.sort-format"));
        assertTrue(configuration.getBool("butterfly.teams.collision"), "nested yaml keys are flattened with dots");
    }
}
