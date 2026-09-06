package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.color.TeamColor;
import net.onelitefeather.butterfly.util.TeamColors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Test
    @DisplayName("Should cover exactly the colors Adventure knows")
    void testSharedColorNamesMatchAdventure() {
        assertEquals(NamedTextColor.NAMES.keys(), Set.copyOf(TeamColors.NAMES),
                "Every Adventure color has to be reachable through a color permission");
        assertEquals(TeamColors.NAMES.size(), Set.copyOf(TeamColors.NAMES).size(),
                "The shared names decide which color permission wins, so they must not repeat");
    }

    @Test
    @DisplayName("Should cover exactly the colors Minestom accepts as a team color")
    void testSharedColorNamesMatchMinestom() {
        // TeamColor#toString is the lowercase protocol name, TeamColor#name the enum constant.
        assertEquals(Arrays.stream(TeamColor.values()).map(TeamColor::toString).collect(Collectors.toSet()),
                Set.copyOf(TeamColors.NAMES),
                "A color permission has to be able to name every team color Minestom accepts");
    }

    @Test
    @DisplayName("Should map every shared color name onto a team color and its text color")
    void testSharedColorNamesResolve() {
        for (String colorName : TeamColors.NAMES) {
            TeamColor teamColor = ButterflyFormat.teamColor(colorName);
            assertEquals(colorName, teamColor.toString(), "Every shared color name has to map onto a team color");
            assertEquals(NamedTextColor.NAMES.value(colorName), teamColor.textColor(),
                    "The name color has to be the text color of the team color");
        }
    }

    @Test
    @DisplayName("Should fall back to white for a color name Minestom does not know")
    void testUnknownColorFallsBackToWhite() {
        assertEquals(TeamColor.WHITE, ButterflyFormat.teamColor("invalid_color"));
        assertEquals(TeamColor.WHITE, ButterflyFormat.teamColor(TeamColors.DEFAULT));
        assertEquals(NamedTextColor.WHITE, ButterflyFormat.teamColor(TeamColors.DEFAULT).textColor());
    }
}
