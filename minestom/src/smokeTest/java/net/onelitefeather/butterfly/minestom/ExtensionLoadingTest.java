package net.onelitefeather.butterfly.minestom;

import net.luckperms.api.LuckPermsRegistration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extensions.ExtensionClassLoader;
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
import java.util.List;
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
    private Path jarInFolder;
    private List<ExtensionClassLoader> loaders;

    @BeforeEach
    void loadExtension(Env env, @TempDir Path extensionsFolder) throws Exception {
        this.env = env;
        luckPerms.addGroup("admin", 100, "<red>[Admin] ", "red");
        luckPerms.addUser(PLAYER_ID, "admin");
        LuckPermsRegistration.register(luckPerms.api());

        String jar = System.getProperty("butterfly.extension.jar");
        assertNotNull(jar, "the build must pass butterfly.extension.jar");
        assertTrue(new File(jar).isFile(), "shadow jar must be built before the test: " + jar);
        jarInFolder = extensionsFolder.resolve("butterfly-minestom.jar");
        Files.copy(Path.of(jar), jarInFolder);

        previousFolder = System.getProperty(FOLDER_PROPERTY);
        System.setProperty(FOLDER_PROPERTY, extensionsFolder.toString());
        manager = new ExtensionManager(env.process());
        manager.start();
        manager.gotoPreInit();
        manager.gotoInit();
        manager.gotoPostInit();
        // shutdown() drops the extensions from the manager, so keep the loaders to close them afterwards
        loaders = manager.getExtensions().stream().map(Extension::getOrigin).map(o -> o.getClassLoader()).toList();
    }

    @AfterEach
    void cleanUp() throws Exception {
        manager.shutdown();
        closeLoaders();
        LuckPermsRegistration.unregister();
        if (previousFolder == null) System.clearProperty(FOLDER_PROPERTY);
        else System.setProperty(FOLDER_PROPERTY, previousFolder);
    }

    /** The loaders keep the jar open; Windows refuses to delete an open file, which would break @TempDir cleanup. */
    private void closeLoaders() throws java.io.IOException {
        for (ExtensionClassLoader loader : loaders) loader.close();
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
    @DisplayName("the shaded jar writes its default config.yaml into the extension data directory")
    void loadedExtensionWritesDefaultConfig() {
        Path dataDirectory = manager.getExtension("Butterfly").getDataDirectory();

        assertTrue(Files.isRegularFile(dataDirectory.resolve("config.yaml")), "relocated avaje-config and the bundled default must work under the extension class loader");
    }

    @Test
    @DisplayName("a config.yaml on the host classpath is not mistaken for Butterfly's bundled defaults")
    void hostConfigIsNotCopiedAsDefaults() throws Exception {
        Path dataDirectory = manager.getExtension("Butterfly").getDataDirectory();

        String written = Files.readString(dataDirectory.resolve("config.yaml"));

        assertTrue(written.contains("sort-format"), "the bundled defaults must be written");
        assertFalse(written.contains("decoy"), "the host's config.yaml must not be copied");
    }

    @Test
    @DisplayName("unloading the extension removes its teams")
    void unloadingRemovesTeams() {
        spawnPlayer();
        assertFalse(MinecraftServer.getTeamManager().getTeams().isEmpty(), "precondition: team exists");

        manager.shutdown();

        assertTrue(MinecraftServer.getTeamManager().getTeams().isEmpty());
    }

    @Test
    @DisplayName("the extension jar is released after shutdown")
    void jarIsReleasedAfterShutdown() throws Exception {
        assertFalse(loaders.isEmpty(), "precondition: the manager created a class loader");

        manager.shutdown();
        closeLoaders();

        assertTrue(Files.deleteIfExists(jarInFolder), "the jar must not stay locked by a class loader");
    }
}
