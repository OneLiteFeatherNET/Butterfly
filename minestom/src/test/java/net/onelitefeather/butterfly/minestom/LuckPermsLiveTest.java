package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.WeightNode;
import net.minestom.server.color.TeamColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.testing.TestConnection;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs Butterfly against a real LuckPerms against a real Minestom server: real groups with
 * real weights, prefixes and permission nodes decide what a really connected client is sent.
 * <p>
 * LuckPerms boots once per JVM, downloads its own dependencies and keeps an H2 database, so
 * this is not part of {@code check}. Run it with {@code ./gradlew :minestom:luckPermsTest}.
 */
@EnvTest
@Tag("luckperms")
class LuckPermsLiveTest {

    private static final Pos SPAWN = new Pos(0, 42, 0);
    private static final AtomicInteger GROUP_ID = new AtomicInteger();

    private static LuckPerms luckPerms;

    /**
     * LuckPerms registers its commands against the running server, so it can only be booted
     * once the harness has built one. It stays up for the rest of the JVM.
     */
    private static synchronized LuckPerms luckPerms() {
        if (luckPerms == null) {
            try {
                Class<?> loaderClass = Class.forName("me.lucko.luckperms.minestom.loader.MinestomLoader");
                Object loader = loaderClass.getMethod("get").invoke(null);
                Object loaded = loaderClass.getMethod("load").invoke(loader);
                loaderClass.getMethod("start").invoke(loaded);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not start LuckPerms", exception);
            }
            luckPerms = LuckPermsProvider.get();
        }
        return luckPerms;
    }

    /**
     * Creates a group the way an administrator would, then a player in it.
     */
    private User playerIn(int weight, String prefix, String colorPermission) {
        String groupName = "butterfly-test-" + GROUP_ID.incrementAndGet();
        LuckPerms luckPerms = luckPerms();
        Group group = luckPerms.getGroupManager().createAndLoadGroup(groupName).join();
        group.data().add(WeightNode.builder(weight).build());
        if (prefix != null) group.data().add(PrefixNode.builder(prefix, weight).build());
        if (colorPermission != null) group.data().add(PermissionNode.builder(colorPermission).build());
        luckPerms.getGroupManager().saveGroup(group).join();

        UUID uuid = UUID.randomUUID();
        User user = luckPerms.getUserManager().loadUser(uuid, "Notch" + GROUP_ID.get()).join();
        user.data().add(InheritanceNode.builder(group).build());
        user.setPrimaryGroup(groupName);
        luckPerms.getUserManager().saveUser(user).join();
        return user;
    }

    private TeamsPacket.Settings settingsOf(TeamsPacket packet) {
        return switch (packet.action()) {
            case TeamsPacket.CreateTeamAction create -> create.settings();
            case TeamsPacket.UpdateTeamAction update -> update.settings();
            default -> throw new AssertionError("Expected team settings, got " + packet.action());
        };
    }

    @Test
    @DisplayName("Should send the real group prefix and permission color to a connected client")
    void testRealGroupReachesClient(Env env) {
        User user = playerIn(100, "<gold>[Admin] ", "color.100.dark_red");
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection(new GameProfile(user.getUniqueId(), user.getUsername()));
        Player player = connection.connect(instance, SPAWN);

        var packets = connection.trackIncoming(TeamsPacket.class);
        new MinestomLuckPermsService().setDisplayName(user);

        assertTrue(packets.collect().stream().anyMatch(packet -> {
            if (!(packet.action() instanceof TeamsPacket.CreateTeamAction)) return false;
            TeamsPacket.Settings settings = settingsOf(packet);
            return settings.color() == TeamColor.DARK_RED
                    && settings.teamPrefix().equals(MiniMessage.miniMessage().deserialize("<gold>[Admin] "));
        }), "The client has to be sent the real prefix and the color permission of the group");

        assertEquals(TeamColor.DARK_RED, player.getTeam().getTeamColor());
        assertTrue(player.getTeam().getMembers().contains(player.getUsername()));
    }

    @Test
    @DisplayName("Should fall back to the color of a real prefix when no color permission is set")
    void testRealPrefixColorFallback(Env env) {
        User user = playerIn(50, "<dark_aqua>[Mod] ", null);
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection(new GameProfile(user.getUniqueId(), user.getUsername()));
        Player player = connection.connect(instance, SPAWN);

        new MinestomLuckPermsService().setDisplayName(user);

        assertEquals(TeamColor.DARK_AQUA, player.getTeam().getTeamColor(),
                "A group with only a colored prefix still has to get a matching team color");
    }

    @Test
    @DisplayName("Should read a real group with no prefix without dropping the team")
    void testRealGroupWithoutPrefix(Env env) {
        User user = playerIn(10, null, "color.10.green");
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection(new GameProfile(user.getUniqueId(), user.getUsername()));
        Player player = connection.connect(instance, SPAWN);

        new MinestomLuckPermsService().setDisplayName(user);

        assertEquals(TeamColor.GREEN, player.getTeam().getTeamColor(),
                "A group without a prefix still has to get its team and color");
    }

    @Test
    @DisplayName("Should lose the color when the group weight no longer matches the permission")
    void testWeightMismatchLosesColor(Env env) {
        // The color node embeds the weight, so re-weighting a group stops it matching.
        User user = playerIn(100, null, "color.999.red");
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection(new GameProfile(user.getUniqueId(), user.getUsername()));
        Player player = connection.connect(instance, SPAWN);

        new MinestomLuckPermsService().setDisplayName(user);

        assertEquals(TeamColor.WHITE, player.getTeam().getTeamColor(),
                "A color node set for another weight must not apply");
    }

    @Test
    @DisplayName("Should read the real prefix and color through the shared API")
    void testSharedApiReadsRealGroup(Env env) {
        User user = playerIn(100, "<light_purple>[Owner] ", null);
        Group group = LuckPermsAPI.luckPermsAPI().getPrimaryGroup(user.getUniqueId());

        assertInstanceOf(Group.class, group, "The primary group of a real user has to resolve");
        assertEquals("<light_purple>[Owner] ", LuckPermsAPI.luckPermsAPI().getGroupPrefix(group).orElse(null));
        assertEquals("light_purple", LuckPermsAPI.luckPermsAPI().getTeamColorName(group));
        assertTrue(LuckPermsAPI.luckPermsAPI().canFormatChat(user.getUniqueId()),
                "Chat formatting stays allowed while the node is undefined");
    }
}
