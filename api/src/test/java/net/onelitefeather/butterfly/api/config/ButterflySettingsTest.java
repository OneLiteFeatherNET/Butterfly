package net.onelitefeather.butterfly.api.config;

import io.avaje.config.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ButterflySettingsTest {

    private final RecordingLogger log = new RecordingLogger();

    private ButterflySettings settingsFrom(Configuration.Builder builder) {
        return ButterflySettings.from(builder.build(), log.logger());
    }

    @Test
    @DisplayName("defaults are a four digit sort format and collision off")
    void defaultsAreDocumented() {
        ButterflySettings defaults = ButterflySettings.defaults();

        assertEquals("%04d", defaults.sortFormat());
        assertFalse(defaults.teamCollision(), "collision defaults to off");
    }

    @Test
    @DisplayName("an empty configuration yields the defaults without a warning")
    void emptyConfigurationYieldsDefaults() {
        ButterflySettings settings = settingsFrom(Configuration.builder());

        assertEquals(ButterflySettings.defaults(), settings);
        assertEquals(List.of(), log.warnings(), "no warning for absent keys");
    }

    @Test
    @DisplayName("configured values are used")
    void configuredValuesAreUsed() {
        ButterflySettings settings = settingsFrom(Configuration.builder()
                .put("butterfly.teams.sort-format", "%02d")
                .put("butterfly.teams.collision", "true"));

        assertEquals("%02d", settings.sortFormat());
        assertTrue(settings.teamCollision(), "collision was switched on");
        assertEquals(List.of(), log.warnings(), "valid values do not warn");
    }

    @Test
    @DisplayName("an unusable sort format falls back to the default and warns with key and value")
    void unusableSortFormatFallsBack() {
        ButterflySettings settings = settingsFrom(Configuration.builder().put("butterfly.teams.sort-format", "%s%s"));

        assertEquals("%04d", settings.sortFormat());
        assertEquals(1, log.warnings().size(), "exactly one warning");
        assertTrue(log.warnings().get(0).contains("butterfly.teams.sort-format"), "warning names the key");
        assertTrue(log.warnings().get(0).contains("%s%s"), "warning names the rejected value");
    }

    @Test
    @DisplayName("a non-boolean collision value falls back to the default and warns with key and value")
    void nonBooleanCollisionFallsBack() {
        ButterflySettings settings = settingsFrom(Configuration.builder().put("butterfly.teams.collision", "sometimes"));

        assertFalse(settings.teamCollision(), "collision falls back to off");
        assertEquals(1, log.warnings().size(), "exactly one warning");
        assertTrue(log.warnings().get(0).contains("butterfly.teams.collision"), "warning names the key");
        assertTrue(log.warnings().get(0).contains("sometimes"), "warning names the rejected value");
    }

    @Test
    @DisplayName("the legacy butterfly.format key is used when the new key is absent")
    void legacyFormatIsUsed() {
        ButterflySettings settings = settingsFrom(Configuration.builder().put("butterfly.format", "%02d"));

        assertEquals("%02d", settings.sortFormat());
    }

    @Test
    @DisplayName("butterfly.teams.sort-format wins over the legacy butterfly.format key")
    void newKeyBeatsLegacyKey() {
        ButterflySettings settings = settingsFrom(Configuration.builder()
                .put("butterfly.format", "%02d")
                .put("butterfly.teams.sort-format", "%05d"));

        assertEquals("%05d", settings.sortFormat());
    }

    @Test
    @DisplayName("an unusable legacy format falls back to the default and warns with the legacy key")
    void unusableLegacyFormatFallsBack() {
        ButterflySettings settings = settingsFrom(Configuration.builder().put("butterfly.format", "nope%s%s"));

        assertEquals("%04d", settings.sortFormat());
        assertTrue(log.warnings().get(0).contains("butterfly.format"), "warning names the legacy key");
    }
}
