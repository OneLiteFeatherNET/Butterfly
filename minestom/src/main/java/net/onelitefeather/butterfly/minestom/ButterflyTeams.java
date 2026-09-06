package net.onelitefeather.butterfly.minestom;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Keeps the scoreboard team of a group in sync with every client.
 * <p>
 * Minestom splits a team's state from the packets that carry it, and getting either half
 * wrong leaves the team correct on the server while no client ever hears about it:
 * <ul>
 *     <li>{@code Team#set...} only writes the field. Only {@code Team#sendUpdatePacket}
 *     and the {@code Team#update...} wrappers put a change on the wire.</li>
 *     <li>Building a team announces it to everyone right away, so it has to be fully
 *     configured beforehand. Anything set afterwards needs its own update.</li>
 *     <li>The two packet actions are not interchangeable. A client drops an update for a
 *     team it has never seen, so a team may only be announced by being built.</li>
 * </ul>
 */
final class ButterflyTeams {

    static final TeamsPacket.NameTagVisibility NAME_TAG_VISIBILITY = TeamsPacket.NameTagVisibility.ALWAYS;

    /**
     * Guards the get-or-create of a team. LuckPerms dispatches its events on its own
     * threads, so two players of the same group joining together would otherwise race
     * into creating the same team twice.
     */
    private final Object teamLock = new Object();

    /**
     * Creates or updates the scoreboard team of a group and makes every online client
     * aware of it.
     *
     * @param teamName      the name of the team
     * @param format        the rendered group to apply
     * @param collisionRule how players of the team collide
     * @return the team the player belongs in
     */
    @NotNull Team apply(@NotNull String teamName, ButterflyFormat.@NotNull GroupFormat format,
                        TeamsPacket.@NotNull CollisionRule collisionRule) {
        synchronized (this.teamLock) {
            TeamManager teamManager = MinecraftServer.getTeamManager();
            Team team = teamManager.getTeam(teamName);

            if (team == null) {
                // build() broadcasts the team to everyone online and Minestom repeats that
                // to anyone connecting later, so every setting has to be in place first.
                return teamManager.createBuilder(teamName)
                        .nameTagVisibility(NAME_TAG_VISIBILITY)
                        .collisionRule(collisionRule)
                        .prefix(format.prefix())
                        .teamColor(format.teamColor())
                        .build();
            }

            if (isUpToDate(team, format, collisionRule)) return team;

            team.setNameTagVisibility(NAME_TAG_VISIBILITY);
            team.setCollisionRule(collisionRule);
            team.setPrefix(format.prefix());
            team.setTeamColor(format.teamColor());
            team.sendUpdatePacket();
            return team;
        }
    }

    /**
     * @return whether the team already carries the wanted settings, so no packet has to be sent
     */
    private boolean isUpToDate(@NotNull Team team, ButterflyFormat.@NotNull GroupFormat format,
                               TeamsPacket.@NotNull CollisionRule collisionRule) {
        return team.getNameTagVisibility() == NAME_TAG_VISIBILITY
                && team.getCollisionRule() == collisionRule
                && team.getTeamColor() == format.teamColor()
                && Objects.equals(team.getPrefix(), format.prefix());
    }
}
