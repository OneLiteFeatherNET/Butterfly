package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.color.TeamColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TeamColorResolutionTest {

    @Test
    @DisplayName("Should resolve all Adventure NamedTextColor keys to a valid Minestom TeamColor")
    void testAllNamedTextColorsMapToTeamColor() {
        for (String colorName : NamedTextColor.NAMES.keys()) {
            TeamColor teamColor = TeamColor.fromName(colorName);
            assertNotNull(teamColor, "TeamColor should not be null for color name: " + colorName);
            assertNotNull(teamColor.textColor(), "TeamColor.textColor() should return a valid TextColor for " + colorName);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"red", "RED", "Red", "dArK_bLuE"})
    @DisplayName("Should resolve color names case-insensitively")
    void testCaseInsensitiveResolution(String colorName) {
        TeamColor teamColor = TeamColor.fromName(colorName);
        assertNotNull(teamColor, "TeamColor should be resolved regardless of casing for: " + colorName);
    }

    @Test
    @DisplayName("Should return null for unknown color names")
    void testUnknownColorReturnsNull() {
        TeamColor teamColor = TeamColor.fromName("invalid_color");
        assertNull(teamColor, "TeamColor should be null for invalid color names");
    }
}
