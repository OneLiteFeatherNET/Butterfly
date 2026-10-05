# Proposal

Ships under Conventional Commit `feat(minestom): ship butterfly-minestom as a minestom extension` (non-breaking, one type).

## Why

Titan (the OneLiteFeather lobby server, Minestom 2026.08.28-26.2, Java 25) dropped Butterfly in 2.0 and now lacks tab list teams, nametag prefixes and the chat format. Titan loads extensions from `extensions/` via `net.onelitefeather:minestom-extensions` 2.2.0, but `butterfly-minestom` is only a library: dropping the jar into `extensions/` crashes the server with "Missing extension.json in extension butterfly-minestom-1.0.23.jar".

## What Changes

- `butterfly-minestom` gains an extension entry point (`ButterflyExtension`) and a generated `extension.json`; the same jar stays usable as a library.
- The extension registers its listeners on a child `EventNode` and removes listeners and created teams on `terminate()`.
- LuckPerms is accessed only from `initialize()`; if it is unavailable the extension logs an error and stays inactive.
- Togglz feature-manager lookup is made independent of the thread context classloader; flags are read from the extension data directory with defaults when absent.
- Build: catalog entries for `minestom-extensions` (+ BOM, processor), annotation processor, compiler argument for the extension version.
- README gains a "Minestom extension" section.
- Not breaking: no existing API is removed or changed.

## Capabilities

### New Capabilities
- `minestom-extension`: Butterfly running as a Minestom extension (loading, lifecycle, LuckPerms availability, tab list teams, nametag prefix/colour, chat format, library compatibility).

### Modified Capabilities

## Impact

- Code: `minestom/` module (new extension class, lifecycle handling of `Butterfly`, Togglz setup), version catalog in `settings.gradle.kts`, `minestom/build.gradle.kts`, README.
- Dependencies: `net.onelitefeather:minestom-extensions` (compileOnly) and its processor (annotationProcessor), resolved from the OneLiteFeather repository.
- Consumers: Titan can load one Butterfly jar from `extensions/`; library users are unaffected.
- Out of scope: below-name scoreboard objective, CloudNet integration, YAML config, commands.
