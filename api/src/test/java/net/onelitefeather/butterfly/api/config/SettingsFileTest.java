package net.onelitefeather.butterfly.api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsFileTest {

    private static final String DEFAULT_RESOURCE = "test-defaults.yaml";

    private final RecordingLogger log = new RecordingLogger();

    private ButterflySettings load(Path dataFolder) {
        return load(dataFolder, dataFolder.resolveSibling("no-such-flags.properties"), Map.of());
    }

    private ButterflySettings load(Path dataFolder, Path legacyFlags, Map<String, String> systemProperties) {
        return SettingsFile.load(dataFolder, DEFAULT_RESOURCE, legacyFlags, log.logger(), systemProperties);
    }

    private static String bundledDefaults() throws IOException {
        try (var in = SettingsFileTest.class.getClassLoader().getResourceAsStream(DEFAULT_RESOURCE)) {
            return new String(in.readAllBytes());
        }
    }

    @Test
    @DisplayName("the default file is written when config.yaml is absent")
    void defaultFileIsWritten(@TempDir Path root) throws IOException {
        Path dataFolder = root.resolve("Butterfly");
        Files.createDirectories(dataFolder);

        load(dataFolder);

        assertEquals(bundledDefaults(), Files.readString(dataFolder.resolve("config.yaml")));
    }

    @Test
    @DisplayName("a missing data folder is created")
    void dataFolderIsCreated(@TempDir Path root) {
        Path dataFolder = root.resolve("extensions").resolve("Butterfly");

        load(dataFolder);

        assertTrue(Files.isRegularFile(dataFolder.resolve("config.yaml")), "config.yaml must exist inside the created folder");
    }

    @Test
    @DisplayName("the freshly written default file yields the default settings without a warning")
    void freshDefaultFileYieldsDefaults(@TempDir Path root) {
        ButterflySettings settings = load(root.resolve("Butterfly"));

        assertEquals(ButterflySettings.defaults(), settings);
        assertEquals(List.of(), log.warnings(), "no warning on a clean first start");
    }

    @Test
    @DisplayName("an existing config.yaml is applied and left untouched")
    void existingFileIsKept(@TempDir Path dataFolder) throws IOException {
        String custom = "butterfly:\n  teams:\n    sort-format: \"%03d\"\n    collision: true\n";
        Path file = Files.writeString(dataFolder.resolve("config.yaml"), custom);

        ButterflySettings settings = load(dataFolder);

        assertEquals(new ButterflySettings("%03d", true, true), settings);
        assertEquals(custom, Files.readString(file), "the file content must not change");
    }

    @Test
    @DisplayName("an empty config.yaml yields the defaults")
    void emptyFileYieldsDefaults(@TempDir Path dataFolder) throws IOException {
        Files.writeString(dataFolder.resolve("config.yaml"), "");

        assertEquals(ButterflySettings.defaults(), load(dataFolder));
    }

    @Test
    @DisplayName("a system property overrides the value from the file")
    void systemPropertyOverridesFile(@TempDir Path dataFolder) throws IOException {
        Files.writeString(dataFolder.resolve("config.yaml"), "butterfly:\n  teams:\n    sort-format: \"%03d\"\n");

        ButterflySettings settings = load(dataFolder, dataFolder.resolve("no-flags"), Map.of("butterfly.teams.sort-format", "%05d"));

        assertEquals("%05d", settings.sortFormat());
    }

    @Test
    @DisplayName("the legacy butterfly.format system property sets the sort format")
    void legacySystemPropertyIsHonoured(@TempDir Path dataFolder) {
        ButterflySettings settings = load(dataFolder, dataFolder.resolve("no-flags"), Map.of("butterfly.format", "%02d"));

        assertEquals("%02d", settings.sortFormat());
    }

    @Test
    @DisplayName("the legacy butterfly.format system property beats the file value")
    void legacySystemPropertyBeatsFile(@TempDir Path dataFolder) throws IOException {
        Files.writeString(dataFolder.resolve("config.yaml"), "butterfly:\n  teams:\n    sort-format: \"%03d\"\n");

        ButterflySettings settings = load(dataFolder, dataFolder.resolve("no-flags"), Map.of("butterfly.format", "%02d"));

        assertEquals("%02d", settings.sortFormat());
    }

    @Test
    @DisplayName("butterfly.teams.sort-format as system property beats the legacy system property")
    void newSystemPropertyBeatsLegacy(@TempDir Path dataFolder) {
        ButterflySettings settings = load(dataFolder, dataFolder.resolve("no-flags"),
                Map.of("butterfly.format", "%02d", "butterfly.teams.sort-format", "%05d"));

        assertEquals("%05d", settings.sortFormat());
    }

    @Test
    @DisplayName("an unusable data folder yields the defaults with a single warning naming the folder")
    void unusableDataFolderFallsBackToDefaults(@TempDir Path root) throws IOException {
        Path blocker = Files.writeString(root.resolve("blocker"), "not a directory");
        Path dataFolder = blocker.resolve("Butterfly");

        ButterflySettings settings = load(dataFolder);

        assertEquals(ButterflySettings.defaults(), settings);
        assertEquals(1, log.warnings().size(), "exactly one warning");
        assertTrue(log.warnings().get(0).contains(dataFolder.toString()), "warning names the folder");
        assertEquals(List.of(), log.errors(), "no error may be logged");
    }

    @Test
    @DisplayName("a legacy flags.properties yields one warning naming the replacement key and stays unchanged")
    void legacyFlagsFileIsReported(@TempDir Path root) throws IOException {
        Path flags = Files.writeString(root.resolve("flags.properties"), "TEAM_COLLISION=true\n");

        ButterflySettings settings = load(root.resolve("Butterfly"), flags, Map.of());

        assertEquals(1, log.warnings().size(), "exactly one warning");
        assertTrue(log.warnings().get(0).contains("flags.properties"), "warning names the old file");
        assertTrue(log.warnings().get(0).contains("butterfly.teams.collision"), "warning names the replacement key");
        assertEquals("TEAM_COLLISION=true\n", Files.readString(flags), "the legacy file must not be modified");
        assertFalse(settings.teamCollision(), "the legacy file must not be read");
    }

    @Test
    @DisplayName("no legacy flags.properties means no warning")
    void noLegacyFlagsFileNoWarning(@TempDir Path root) {
        load(root.resolve("Butterfly"));

        assertEquals(List.of(), log.warnings(), "nothing to warn about");
    }

    @Test
    @DisplayName("a config.yaml in another directory is ignored")
    void configInOtherDirectoryIsIgnored(@TempDir Path root) throws IOException {
        Path other = Files.createDirectories(root.resolve("other"));
        Files.writeString(other.resolve("config.yaml"), "butterfly:\n  teams:\n    sort-format: \"%decoy\"\n");
        Files.writeString(other.resolve("application.yaml"), "butterfly:\n  teams:\n    sort-format: \"%decoy\"\n");

        ButterflySettings settings = load(root.resolve("Butterfly"));

        assertEquals("%04d", settings.sortFormat());
    }

    @Test
    @DisplayName("without a file the defaults apply")
    void fromSystemPropertiesDefaults() {
        assertEquals(ButterflySettings.defaults(), SettingsFile.fromSystemProperties(Map.of(), log.logger()));
    }

    @Test
    @DisplayName("without a file the system properties are applied")
    void fromSystemPropertiesApplied() {
        ButterflySettings settings = SettingsFile.fromSystemProperties(
                Map.of("butterfly.format", "%02d", "butterfly.teams.collision", "true"), log.logger());

        assertEquals(new ButterflySettings("%02d", true, true), settings);
    }
}
