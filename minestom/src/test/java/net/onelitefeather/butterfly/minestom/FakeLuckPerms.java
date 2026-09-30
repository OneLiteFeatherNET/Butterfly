package net.onelitefeather.butterfly.minestom;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.cacheddata.Result;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.query.QueryOptionsRegistry;
import net.luckperms.api.util.Tristate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory LuckPerms API used instead of a real LuckPerms installation.
 */
final class FakeLuckPerms {

    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<UUID, User> users = new LinkedHashMap<>();
    private final AtomicInteger openSubscriptions = new AtomicInteger();
    private final LuckPerms api = createApi();

    LuckPerms api() {
        return api;
    }

    int openSubscriptions() {
        return openSubscriptions.get();
    }

    /**
     * @param color name of the team colour granted through the {@code color.<weight>.<name>} permission, may be null
     */
    Group addGroup(String name, int weight, String prefix, String color) {
        CachedMetaData meta = Stubs.of(CachedMetaData.class, Map.of("getPrefix", _ -> prefix));
        CachedPermissionData permissions = Stubs.of(CachedPermissionData.class, Map.of("queryPermission", args -> {
            boolean granted = color != null && args[0].equals("color.%s.%s".formatted(weight, color));
            return Stubs.of(Result.class, Map.of("result", _ -> Tristate.of(granted)));
        }));
        CachedDataManager data = Stubs.of(CachedDataManager.class, Map.of(
                "getMetaData", _ -> meta,
                "getPermissionData", _ -> permissions));
        Group group = Stubs.of(Group.class, Map.of(
                "getName", _ -> name,
                "getWeight", _ -> OptionalInt.of(weight),
                "getCachedData", _ -> data));
        groups.put(name, group);
        return group;
    }

    void addUser(UUID uuid, String primaryGroup) {
        users.put(uuid, Stubs.of(User.class, Map.of(
                "getUniqueId", _ -> uuid,
                "getPrimaryGroup", _ -> primaryGroup)));
    }

    private LuckPerms createApi() {
        UserManager userManager = Stubs.of(UserManager.class, Map.of("getUser", args -> users.get((UUID) args[0])));
        GroupManager groupManager = Stubs.of(GroupManager.class, Map.of(
                "getGroup", args -> groups.get((String) args[0]),
                "getLoadedGroups", _ -> new java.util.LinkedHashSet<>(groups.values())));
        EventBus eventBus = Stubs.of(EventBus.class, Map.of("subscribe", _ -> {
            openSubscriptions.incrementAndGet();
            return Stubs.of(EventSubscription.class, Map.of("close", _ -> {
                openSubscriptions.decrementAndGet();
                return null;
            }));
        }));
        QueryOptions options = Stubs.of(QueryOptions.class, Map.of());
        QueryOptionsRegistry registry = Stubs.of(QueryOptionsRegistry.class, Map.of("defaultContextualOptions", _ -> options));
        return Stubs.of(LuckPerms.class, Map.of(
                "getQueryOptionsRegistry", _ -> registry,
                "getUserManager", _ -> userManager,
                "getGroupManager", _ -> groupManager,
                "getEventBus", _ -> eventBus));
    }
}
