package net.onelitefeather.butterfly.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamColorsTest {

    @Test
    @DisplayName("Should expose the sixteen vanilla team colors")
    void testColorNames() {
        assertEquals(16, TeamColors.NAMES.size());
        assertTrue(TeamColors.NAMES.contains(TeamColors.DEFAULT), "The default color has to be a valid team color");
    }

    @ParameterizedTest
    @CsvSource({
            "<red>[Admin] ,red",
            "<dark_blue>[Mod] ,dark_blue",
            "<color:gold>[VIP] ,gold",
            "<colour:gold>[VIP] ,gold",
            "<c:aqua>[VIP] ,aqua",
            "<RED>[Admin] ,red",
            "<gray>[<red>Admin<gray>] ,gray",
            "<red><bold>[Admin] ,red",
            "<red>[Admin]</red> <yellow>,yellow"
    })
    @DisplayName("Should resolve the color a prefix leaves in effect")
    void testTrailingColor(String prefix, String expected) {
        assertEquals(Optional.of(expected), TeamColors.trailingColor(prefix));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "[Admin] ",
            "<#ff0000>[Admin] ",
            "<gradient:red:blue>[Admin] ",
            "<rainbow>[Admin] ",
            "<bold>[Admin] ",
            "\\<red>[Admin] ",
            "<red>[Admin]</red> ",
            "<red>[Admin]</color> ",
            "<red>[Admin]</c> ",
            "<red>[Admin]<reset> "
    })
    @DisplayName("Should resolve no color when the prefix leaves none in effect")
    void testNoTrailingColor(String prefix) {
        assertEquals(Optional.empty(), TeamColors.trailingColor(prefix));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should tolerate a missing prefix")
    void testMissingPrefix(String prefix) {
        assertEquals(Optional.empty(), TeamColors.trailingColor(prefix));
    }
}
