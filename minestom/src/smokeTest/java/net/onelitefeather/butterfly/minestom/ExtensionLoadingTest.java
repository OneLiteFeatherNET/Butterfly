package net.onelitefeather.butterfly.minestom;

import net.luckperms.api.LuckPermsRegistration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.extensions.ExtensionManager;
import net.minestom.server.network.player.GameProfile;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Loads the built shadow jar through Minestom's own extension loading path, the way a host such as Titan
 * does: jar in the extensions folder, then start, pre-init, init. LuckPerms is a fake registered in the host.
 * Butterfly's classes are deliberately not on this source set's classpath, so they can only come from the jar.
 */
@EnvTest
class ExtensionLoadingTest {

    private static final String FOLDER_PROPERTY = "minestom.extension.folder";
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final FakeLuckPerms luckPerms = new FakeLuckPerms();
    private ExtensionManager manager;
    private String previousFolder;
    private Env env;

    @BeforeEach
    void loadExtension(Env env, @TempDir Path extensionsFolder) throws Exception {
        this.env = env;
        luckPerms.addGroup("admin", 100, "<red>[Admin] ", "red");
        luckPerms.addUser(PLAYER_ID, "admin");
        LuckPermsRegistration.register(luckPerms.api());

        String jar = System.getProperty("butterfly.extension.jar");
        assertNotNull(jar, "the build must pass butterfly.extension.jar");
        assertTrue(new File(jar).isFile(), "shadow jar must be built before the test: " + jar);
        Files.copy(Path.of(jar), extensionsFolder.resolve("butterfly-minestom.jar"));

        previousFolder = System.getProperty(FOLDER_PROPERTY);
        System.setProperty(FOLDER_PROPERTY, extensionsFolder.toString());
        manager = new ExtensionManager(env.process());
        manager.start();
        manager.gotoPreInit();
        manager.gotoInit();
        manager.gotoPostInit();
    }

    @AfterEach
    void cleanUp() {
        manager.shutdown();
        LuckPermsRegistration.unregister();
        if (previousFolder == null) System.clearProperty(FOLDER_PROPERTY);
        else System.setProperty(FOLDER_PROPERTY, previousFolder);
    }

    private Player spawnPlayer() {
        Player player = env.createConnection(new GameProfile(PLAYER_ID, "Alice")).connect(env.createFlatInstance(), Pos.ZERO);
        env.tick();
        return player;
    }

    @Test
    @DisplayName("the extension manager registers Butterfly although no LuckPerms extension exists")
    void managerRegistersExtension() {
        assertNotNull(manager.getExtension("Butterfly"));
    }

    @Test
    @DisplayName("the entry point is loaded by the extension class loader")
    void entryPointComesFromExtensionLoader() {
        ClassLoader loader = manager.getExtension("Butterfly").getClass().getClassLoader();

        assertEquals("Ext_Butterfly", loader.getName());
    }

    @Test
    @DisplayName("a loaded extension handles spawning players")
    void loadedExtensionHandlesSpawn() {
        Player player = spawnPlayer();

        assertNotNull(player.getTeam(), "the spawn listener assigned a team");
        assertEquals("0001admin", player.getTeam().getTeamName());
    }

    @Test
    @DisplayName("unloading the extension removes its teams")
    void unloadingRemovesTeams() {
        spawnPlayer();
        assertFalse(MinecraftServer.getTeamManager().getTeams().isEmpty(), "precondition: team exists");

        manager.shutdown();

        assertTrue(MinecraftServer.getTeamManager().getTeams().isEmpty());
    }
}
