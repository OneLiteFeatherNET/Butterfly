package net.onelitefeather.butterfly.minestom;

import net.minestom.server.extensions.Extension;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreInitializationTest {

    @Test
    @DisplayName("preInitialize loads no LuckPerms class and no Butterfly API class")
    void preInitializeTouchesNoLuckPerms() throws Exception {
        URL[] urls = {IsolatedClassLoader.locationOf(ButterflyExtension.class), IsolatedClassLoader.locationOf(LuckPermsAPI.class)};
        try (var loader = new IsolatedClassLoader(urls, getClass().getClassLoader(), "net.luckperms.")) {
            Class<?> type = Class.forName(ButterflyExtension.class.getName(), true, loader);
            assertEquals(loader, type.getClassLoader(), "the extension must come from the isolated loader");
            Extension extension = (Extension) type.getDeclaredConstructor().newInstance();

            assertDoesNotThrow(extension::preInitialize);

            assertTrue(loader.requestedClasses().stream().noneMatch(name -> name.startsWith("net.luckperms.")),
                    "LuckPerms classes requested: " + loader.requestedClasses());
            assertTrue(loader.requestedClasses().stream().noneMatch(name -> name.startsWith("net.onelitefeather.butterfly.api.")),
                    "api classes (static LuckPerms lookup) requested: " + loader.requestedClasses());
        }
    }
}
