package net.onelitefeather.butterfly.api;

import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.cacheddata.Result;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.util.Tristate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers how Butterfly reads a LuckPerms group: which permission node decides a team color,
 * what happens when a group only carries a colored prefix, and how an undefined permission is
 * treated. These are the rules a server's own group setup has to line up with.
 */
class LuckPermsDataTest {

    private static Result<Tristate, ?> result(Tristate tristate) {
        Result<Tristate, ?> result = mock();
        when(result.result()).thenReturn(tristate);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Group group(int weight, String prefix, String grantedPermission) {
        Group group = mock(RETURNS_DEEP_STUBS);
        CachedDataManager data = mock();
        CachedPermissionData permissionData = mock();
        CachedMetaData metaData = mock();

        when(group.getWeight()).thenReturn(weight < 0 ? OptionalInt.empty() : OptionalInt.of(weight));
        when(group.getCachedData()).thenReturn(data);
        when(data.getPermissionData()).thenReturn(permissionData);
        when(data.getMetaData()).thenReturn(metaData);
        when(metaData.getPrefix()).thenReturn(prefix);
        when(permissionData.queryPermission(anyString()))
                .thenAnswer(invocation -> result(invocation.getArgument(0).equals(grantedPermission)
                        ? Tristate.TRUE : Tristate.UNDEFINED));
        return group;
    }

    private static User user(Tristate chatFormatting) {
        User user = mock();
        CachedDataManager data = mock();
        CachedPermissionData permissionData = mock();
        when(user.getCachedData()).thenReturn(data);
        when(data.getPermissionData()).thenReturn(permissionData);
        when(permissionData.queryPermission("butterfly.chat.format")).thenAnswer(i -> result(chatFormatting));
        return user;
    }

    @Test
    @DisplayName("Should read the team color from the weighted color permission")
    void testColorPermissionWins() {
        assertEquals("red", LuckPermsData.teamColorName(group(100, "<gold>[Admin] ", "color.100.red")),
                "An explicit color permission has to beat the prefix color");
    }

    @Test
    @DisplayName("Should key the color permission on the group weight")
    void testColorPermissionUsesWeight() {
        // A node set for another weight must not match, which is what makes changing a group
        // weight silently drop its color.
        assertEquals("white", LuckPermsData.teamColorName(group(50, null, "color.100.red")));
        assertEquals("red", LuckPermsData.teamColorName(group(50, null, "color.50.red")));
    }

    @Test
    @DisplayName("Should use a weight of -1 for a group without one")
    void testColorPermissionWithoutWeight() {
        assertEquals("red", LuckPermsData.teamColorName(group(-1, null, "color.-1.red")));
    }

    @Test
    @DisplayName("Should fall back to the color the prefix leaves in effect")
    void testPrefixColorFallback() {
        assertEquals("gold", LuckPermsData.teamColorName(group(100, "<gold>[VIP] ", null)),
                "A group with only a colored prefix still has to get a matching team color");
        assertEquals("gray", LuckPermsData.teamColorName(group(100, "<gray>[<red>Owner<gray>] ", null)),
                "The color the name inherits is the one the prefix ends on");
    }

    @Test
    @DisplayName("Should fall back to white without a color permission or a colored prefix")
    void testDefaultColor() {
        assertEquals("white", LuckPermsData.teamColorName(group(100, "[Member] ", null)));
        assertEquals("white", LuckPermsData.teamColorName(group(100, null, null)));
        assertEquals("white", LuckPermsData.teamColorName(null));
    }

    @Test
    @DisplayName("Should read the prefix of a group")
    void testPrefix() {
        assertEquals("<red>[Admin] ", LuckPermsData.prefix(group(100, "<red>[Admin] ", null)).orElse(null));
        assertTrue(LuckPermsData.prefix(group(100, null, null)).isEmpty(), "A group may define no prefix");
        assertTrue(LuckPermsData.prefix(null).isEmpty(), "The group may be unresolvable");
    }

    @Test
    @DisplayName("Should allow chat formatting unless it is explicitly denied")
    void testChatFormattingDefault() {
        assertTrue(LuckPermsData.canFormatChat(user(Tristate.UNDEFINED)),
                "A server that never set the node has to keep the colored chat it had");
        assertTrue(LuckPermsData.canFormatChat(user(Tristate.TRUE)));
        assertTrue(LuckPermsData.canFormatChat(null), "An unloaded player must not lose chat colors");
        assertFalse(LuckPermsData.canFormatChat(user(Tristate.FALSE)),
                "A negated node has to revoke chat formatting");
    }
}
