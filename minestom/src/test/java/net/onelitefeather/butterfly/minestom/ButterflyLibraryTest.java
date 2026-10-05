package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.onelitefeather.butterfly.api.config.ButterflySettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Butterfly used as a library: the host passes the settings, nothing is read from disk.
 */
@EnvTest
class ButterflyLibraryTest {

    private ExtensionFixture fixture;
    private Butterfly butterfly;

    @AfterEach
    void cleanUp() {
        if (butterfly != null) butterfly.terminate();
        fixture.unregisterLuckPerms();
    }

    private Player spawnWith(Env env, Path dataDirectory, ButterflySettings settings) {
        fixture = new ExtensionFixture(env, dataDirectory);
        fixture.registerLuckPerms();
        butterfly = Butterfly.create(env.process().eventHandler(), settings);
        butterfly.load();
        return fixture.spawnPlayer();
    }

    @Test
    @DisplayName("settings with collision on give the team collision rule ALWAYS")
    void hostEnablesCollision(Env env, @TempDir Path dir) {
        Player player = spawnWith(env, dir, new ButterflySettings("%04d", true, true));

        assertEquals(TeamsPacket.CollisionRule.ALWAYS, player.getTeam().getCollisionRule());
    }

    @Test
    @DisplayName("default settings give the team collision rule NEVER")
    void defaultsDisableCollision(Env env, @TempDir Path dir) {
        Player player = spawnWith(env, dir, ButterflySettings.defaults());

        assertEquals(TeamsPacket.CollisionRule.NEVER, player.getTeam().getCollisionRule());
    }

    @Test
    @DisplayName("a custom sort format names the team")
    void customSortFormatNamesTeam(Env env, @TempDir Path dir) {
        Player player = spawnWith(env, dir, new ButterflySettings("%02d", false, true));

        assertEquals("01admin", player.getTeam().getTeamName());
    }

    @Test
    @DisplayName("settings with the chat head off give a chat line without head and leading space")
    void hostDisablesChatHead(Env env, @TempDir Path dir) {
        Player player = spawnWith(env, dir, new ButterflySettings("%04d", false, false));

        PlayerChatEvent event = fixture.chat(player, "hello");

        assertEquals("[Admin] Alice: hello", PlainTextComponentSerializer.plainText().serialize(event.getFormattedMessage()));
        assertFalse(ExtensionBehaviourTest.containsObject(event.getFormattedMessage()), "no player head in the line");
    }

    @Test
    @DisplayName("default settings give a chat line that starts with the head")
    void defaultsShowChatHead(Env env, @TempDir Path dir) {
        Player player = spawnWith(env, dir, ButterflySettings.defaults());

        PlayerChatEvent event = fixture.chat(player, "hello");

        assertNotNull(ExtensionBehaviourTest.headOf(event.getFormattedMessage()), "the line starts with the head");
    }

    @Test
    @DisplayName("create() without settings writes no file into the working directory")
    void createWithoutSettingsWritesNoFile(Env env, @TempDir Path dir) throws IOException {
        fixture = new ExtensionFixture(env, dir);
        Path workingDirectory = Path.of("").toAbsolutePath();
        List<String> before = listing(workingDirectory);

        Butterfly.create();

        assertEquals(before, listing(workingDirectory), "the working directory must be unchanged");
    }

    private static List<String> listing(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.map(path -> path.getFileName().toString()).sorted().toList();
        }
    }
}
