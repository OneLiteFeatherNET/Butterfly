# Design

## Context

- The `minestom/` module today exposes `Butterfly.create().load()`, which registers a `PlayerChatEvent` listener (MiniMessage group prefix + message) and a `PlayerSpawnEvent` listener (Minestom `Team` per LuckPerms group, name `%04d` + group, `TeamColor`, prefix) on the global event handler, plus `terminate()`. `LuckPermsAPIImplementation` holds `static final LuckPerms LUCK_PERMS = LuckPermsProvider.get()` (in the `api` module).
- LuckPerms API, Minestom and MiniMessage are `compileOnly`; the `api` module and Togglz 4.6.4 are shaded (`shadowJar` -> `butterfly-minestom.jar`). Togglz finds its `SingletonFeatureManagerProvider` through `META-INF/services` and reads `flags.properties`.
- Titan loads extensions with `minestom-extensions` 2.2.0. `ExtensionClassLoader extends URLClassLoader` with parent `MinecraftServer.class.getClassLoader()`, so `net.minestom.*`, `net.kyori.adventure.*` and `net.luckperms.api.*` resolve from the host.
- In Titan 2.x LuckPerms runs in-process (not an extension). `LuckPermsProvider` is registered before extension `initialize()`; `preInitialize()` runs earlier, before LuckPerms is up.
- An earlier attempt (local branch `feat/minestom-extension`, `ButterflyExtension` with `@ExtensionInfo(dependencies = {"LuckPerms"})`) is a reference only and conflicts with main.

## Goals / Non-Goals

**Goals:**
- One jar that Minestom's extension manager can load and that behaves exactly like the library today.
- Safe lifecycle: no crash when LuckPerms is missing, full cleanup on terminate.

**Non-Goals:**
- Below-name scoreboard objective, CloudNet integration, YAML config, commands.
- Removing or changing the library API.

## Decisions

1. **Same artifact gains the extension entry point.** `butterfly-minestom` keeps its coordinates, adds `ButterflyExtension` and a generated `extension.json`, and remains a usable library. Not breaking. Rejected: a separate `butterfly-minestom-extension` artifact (two jars; the reported crash shows people drop whichever jar they have into `extensions/`); removing library use (breaking, no benefit).
2. **No `LuckPerms` dependency in `extension.json`.** LuckPerms is not an extension in Titan 2.x, so declaring it would make the extension manager refuse to load Butterfly. Rejected: copying `dependencies = {"LuckPerms"}` from the earlier attempt.
3. **LuckPerms touched only in `initialize()` or later.** `preInitialize()` must not reference `LuckPermsProvider` (nor classes whose static initialisers do, e.g. `LuckPermsAPIImplementation`). If LuckPerms is unavailable in `initialize()`, log a clear error and stay inactive rather than crash the server. Rejected: failing the extension load (takes the server down for a cosmetic feature).
4. **Listeners live on a child `EventNode`.** The extension adds one node to the global handler and removes it in `terminate()`, which also removes teams it created. Rejected: registering directly on the global handler (cannot be unregistered as a unit).
5. **Scope equals the library**: tab list sorting teams, nametag prefix/colour, chat format. Behaviour identical; the extension reuses the library code paths.
6. **Togglz under the extension classloader.** Togglz may resolve `SingletonFeatureManagerProvider` via the thread context classloader and miss the provider inside the extension classloader. Set the feature manager explicitly (or bind the context classloader around lookup) and read `flags.properties` from the extension data directory (`extensions/Butterfly/flags.properties`), using defaults when the file is absent. Verified by a test that loads the extension through an isolated classloader.
7. **Docs.** README section "Minestom extension": put exactly one Butterfly jar (at least the release containing this change) into `extensions/`; do not also shade or depend on it in the host.

## Risks / Trade-offs

- Togglz classloader lookup (decision 6) is the main unknown; mitigated by test and explicit setup.
- The static `LUCK_PERMS = LuckPermsProvider.get()` in the `api` module initialises on first class use; the extension must not load that class before `initialize()`. Mitigated by decision 3 and a test.
- Two copies of Butterfly (host-shaded plus extension jar) would conflict; documented in README, not enforced.
- Toolchain: the project already builds on Java 25, which minestom-extensions 2.2.0 requires; no toolchain change needed.
