package net.onelitefeather.butterfly.api;

import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.util.Tristate;
import net.onelitefeather.butterfly.util.Constants;
import net.onelitefeather.butterfly.util.TeamColors;

import java.util.Optional;

/**
 * Reads the LuckPerms data Butterfly displays.
 * <p>
 * Kept apart from {@link LuckPermsAPI} so the rules below can be exercised against a group
 * without a running LuckPerms: everything here works off the holder's own cached data and
 * never reaches for the provider.
 */
final class LuckPermsData {

    private LuckPermsData() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param group the group to read, may be {@code null}
     * @return the prefix of the group, empty when it defines none
     */
    static Optional<String> prefix(Group group) {
        if (group == null) return Optional.empty();
        return Optional.ofNullable(group.getCachedData().getMetaData().getPrefix());
    }

    /**
     * Resolves the team color of a group.
     * <p>
     * An explicit {@code color.<weight>.<name>} permission wins. Groups that only define a
     * colored prefix fall back to the color their prefix leaves in effect, so the name above
     * a player's head matches the name in chat without a second permission node.
     *
     * @param group the group to read, may be {@code null}
     * @return the color name, never {@code null}
     */
    static String teamColorName(Group group) {
        if (group == null) return TeamColors.DEFAULT;

        int weight = group.getWeight().orElse(-1);
        CachedPermissionData permissionData = group.getCachedData().getPermissionData();
        for (String colorName : TeamColors.NAMES) {
            String permission = Constants.TEAM_COLOR_PERMISSION.formatted(weight, colorName);
            if (permissionData.queryPermission(permission).result() == Tristate.TRUE) {
                return colorName;
            }
        }

        return prefix(group).flatMap(TeamColors::trailingColor).orElse(TeamColors.DEFAULT);
    }

    /**
     * Whether a player may use MiniMessage formatting in chat. Allowed unless
     * {@code butterfly.chat.format} is explicitly denied, so a server that never set the node
     * keeps the colored chat it had.
     *
     * @param user the player to check, may be {@code null}
     * @return whether the player may format their chat
     */
    static boolean canFormatChat(User user) {
        if (user == null) return true;
        return user.getCachedData().getPermissionData()
                .queryPermission(Constants.CHAT_FORMATTING_PERMISSION).result() != Tristate.FALSE;
    }
}
