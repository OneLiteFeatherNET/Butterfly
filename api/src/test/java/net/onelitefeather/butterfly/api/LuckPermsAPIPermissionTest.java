package net.onelitefeather.butterfly.api;

import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.cacheddata.Result;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuckPermsAPIPermissionTest {

    private final LuckPermsAPI api = LuckPermsAPI.luckPermsAPI();
    private final QueryOptions userOptions = stub(QueryOptions.class, (_, _) -> null);
    private final List<QueryOptions> queriedWith = new CopyOnWriteArrayList<>();

    @Test
    @DisplayName("a permission the user's cached data grants is reported as granted")
    void grantedPermissionIsTrue() {
        User user = userWith(Set.of("butterfly.chat.tag.color"));

        assertTrue(api.hasPermission(user, "butterfly.chat.tag.color"), "granted node must be true");
    }

    @Test
    @DisplayName("a permission the user does not have is reported as not granted")
    void missingPermissionIsFalse() {
        User user = userWith(Set.of("butterfly.chat.tag.color"));

        assertFalse(api.hasPermission(user, "butterfly.chat.tag.click"), "ungranted node must be false");
    }

    @Test
    @DisplayName("the check queries the cached permission data with the user's own query options")
    void usesUsersContextualQueryOptions() {
        User user = userWith(Set.of("butterfly.chat.tag.color"));

        api.hasPermission(user, "butterfly.chat.tag.color");

        assertEquals(1, queriedWith.size(), "permission data must be requested exactly once");
        assertSame(userOptions, queriedWith.getFirst(), "the user's query options must be passed through");
    }

    @Test
    @DisplayName("a user that is not loaded has no permission")
    void unloadedUserHasNoPermission() {
        assertFalse(api.hasPermission((User) null, "butterfly.chat.tag.color"), "null user must be false");
    }

    private User userWith(Set<String> granted) {
        CachedPermissionData permissions = stub(CachedPermissionData.class, (name, args) -> {
            if (!name.equals("queryPermission")) return null;
            Tristate state = Tristate.of(granted.contains((String) args[0]));
            return stub(Result.class, (resultMethod, _) -> resultMethod.equals("result") ? state : null);
        });
        CachedDataManager data = stub(CachedDataManager.class, (name, args) -> {
            if (!name.equals("getPermissionData")) return null;
            queriedWith.add((QueryOptions) args[0]);
            return permissions;
        });
        return stub(User.class, (name, _) -> switch (name) {
            case "getCachedData" -> data;
            case "getQueryOptions" -> userOptions;
            default -> null;
        });
    }

    private interface Answer {
        Object answer(String method, Object[] args);
    }

    private static <T> T stub(Class<T> type, Answer answer) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            Object[] arguments = args == null ? new Object[0] : args;
            return switch (method.getName()) {
                case "toString" -> type.getSimpleName() + "Stub";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == arguments[0];
                default -> answer.answer(method.getName(), arguments);
            };
        }));
    }
}
