package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.TeamColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.testing.TestConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives a real Minestom server and asserts on the packets a connected client actually
 * receives, because the bugs this guards against were invisible server side: the team
 * carried the right prefix and color in memory while no client was ever told.
 */
@EnvTest
class ButterflyTeamsIntegrationTest {

    private static final String TEAM_NAME = "0001admin";
    private static final Pos SPAWN = new Pos(0, 42, 0);

    private static ButterflyFormat.GroupFormat format(String prefix, TeamColor color) {
        Component rendered = MiniMessage.miniMessage().deserialize(prefix);
        return new ButterflyFormat.GroupFormat(TEAM_NAME, rendered, color,
                Component.text().append(rendered).append(Component.text("Notch")).build());
    }

    private static TeamsPacket.Settings settingsOf(TeamsPacket packet) {
        return switch (packet.action()) {
            case TeamsPacket.CreateTeamAction create -> create.settings();
            case TeamsPacket.UpdateTeamAction update -> update.settings();
            default -> throw new AssertionError("Expected a team settings packet, got " + packet.action());
        };
    }

    private Player connect(Env env, Instance instance) {
        TestConnection connection = env.createConnection();
        return connection.connect(instance, SPAWN);
    }

    @Test
    @DisplayName("Should announce a new team to a player that is already online")
    void testNewTeamReachesOnlinePlayer(Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        connection.connect(instance, SPAWN);

        var packets = connection.trackIncoming(TeamsPacket.class);
        new ButterflyTeams().apply(TEAM_NAME, format("<red>[Admin] ", TeamColor.RED), TeamsPacket.CollisionRule.NEVER);

        packets.assertSingle(packet -> {
            assertEquals(TEAM_NAME, packet.teamName());
            // An update action for a team the client has never seen is dropped client side,
            // so a brand new team has to arrive as a create action.
            assertInstanceOf(TeamsPacket.CreateTeamAction.class, packet.action(),
                    "A new team has to reach the client as a create action");

            TeamsPacket.Settings settings = settingsOf(packet);
            assertEquals(TeamColor.RED, settings.color(), "The client has to be told the team color");
            assertEquals(MiniMessage.miniMessage().deserialize("<red>[Admin] "), settings.teamPrefix(),
                    "The client has to be told the team prefix");
            assertEquals(TeamsPacket.NameTagVisibility.ALWAYS, settings.nameTagVisibility());
            assertEquals(TeamsPacket.CollisionRule.NEVER, settings.collisionRule());
        });
    }

    @Test
    @DisplayName("Should push a changed color to a player that is already online")
    void testColorChangeReachesOnlinePlayer(Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        connection.connect(instance, SPAWN);

        ButterflyTeams teams = new ButterflyTeams();
        teams.apply(TEAM_NAME, format("<red>[Admin] ", TeamColor.RED), TeamsPacket.CollisionRule.NEVER);

        var packets = connection.trackIncoming(TeamsPacket.class);
        teams.apply(TEAM_NAME, format("<gold>[Admin] ", TeamColor.GOLD), TeamsPacket.CollisionRule.NEVER);

        packets.assertSingle(packet -> {
            assertEquals(TEAM_NAME, packet.teamName());
            assertInstanceOf(TeamsPacket.UpdateTeamAction.class, packet.action(),
                    "A known team has to be changed with an update action");

            TeamsPacket.Settings settings = settingsOf(packet);
            assertEquals(TeamColor.GOLD, settings.color(), "The new color has to reach the client");
            assertEquals(MiniMessage.miniMessage().deserialize("<gold>[Admin] "), settings.teamPrefix(),
                    "The new prefix has to reach the client");
        });
    }

    @Test
    @DisplayName("Should not resend a team that already carries the wanted settings")
    void testUnchangedTeamSendsNothing(Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        connection.connect(instance, SPAWN);

        ButterflyTeams teams = new ButterflyTeams();
        var unchanged = format("<red>[Admin] ", TeamColor.RED);
        teams.apply(TEAM_NAME, unchanged, TeamsPacket.CollisionRule.NEVER);

        var packets = connection.trackIncoming(TeamsPacket.class);
        teams.apply(TEAM_NAME, unchanged, TeamsPacket.CollisionRule.NEVER);
        teams.apply(TEAM_NAME, unchanged, TeamsPacket.CollisionRule.NEVER);

        packets.assertEmpty();
    }

    @Test
    @DisplayName("Should reuse one team for every player of a group")
    void testSameTeamIsReused(Env env) {
        Instance instance = env.createFlatInstance();
        connect(env, instance);

        ButterflyTeams teams = new ButterflyTeams();
        var groupFormat = format("<red>[Admin] ", TeamColor.RED);

        Team first = teams.apply(TEAM_NAME, groupFormat, TeamsPacket.CollisionRule.NEVER);
        Team second = teams.apply(TEAM_NAME, groupFormat, TeamsPacket.CollisionRule.NEVER);

        assertSame(first, second, "A group has to map onto a single team");
        assertEquals(1, MinecraftServer.getTeamManager().getTeams().stream()
                .filter(team -> team.getTeamName().equals(TEAM_NAME)).count());
    }

    @Test
    @DisplayName("Should put the player into the team and colour their name above their head")
    void testPlayerJoinsTeam(Env env) {
        Instance instance = env.createFlatInstance();
        Player player = connect(env, instance);

        ButterflyTeams teams = new ButterflyTeams();
        Team team = teams.apply(TEAM_NAME, format("<red>[Admin] ", TeamColor.RED), TeamsPacket.CollisionRule.NEVER);
        player.setTeam(team);

        assertSame(team, player.getTeam());
        assertTrue(team.getMembers().contains(player.getUsername()),
                "The player has to be a member, or the client shows no team color above them");
        assertEquals(TeamColor.RED, team.getTeamColor());
    }

    @Test
    @DisplayName("Should take a leaving player out of their team")
    void testPlayerLeavesTeam(Env env) {
        Instance instance = env.createFlatInstance();
        Player player = connect(env, instance);

        ButterflyTeams teams = new ButterflyTeams();
        Team team = teams.apply(TEAM_NAME, format("<red>[Admin] ", TeamColor.RED), TeamsPacket.CollisionRule.NEVER);
        player.setTeam(team);
        player.setTeam(null);

        assertTrue(team.getMembers().isEmpty(), "A team must not keep a player that left");
    }

    @Test
    @DisplayName("Should send an existing team to a player that connects later")
    void testTeamReachesLateJoiner(Env env) {
        Instance instance = env.createFlatInstance();
        new ButterflyTeams().apply(TEAM_NAME, format("<red>[Admin] ", TeamColor.RED), TeamsPacket.CollisionRule.NEVER);

        TestConnection connection = env.createConnection();
        var packets = connection.trackIncoming(TeamsPacket.class);
        connection.connect(instance, SPAWN);

        packets.assertAnyMatch(packet -> packet.teamName().equals(TEAM_NAME)
                && packet.action() instanceof TeamsPacket.CreateTeamAction
                && settingsOf(packet).color() == TeamColor.RED);
    }
}
