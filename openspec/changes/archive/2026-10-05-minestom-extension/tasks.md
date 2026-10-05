# Tasks

## 1. Build

- [x] 1.1 Add `minestom-extensions` (BOM, library, processor) to the version catalog and the OneLiteFeather repository to `settings.gradle.kts` (see `feat/minestom-extension` for coordinates)
- [x] 1.2 In `minestom/build.gradle.kts` add `compileOnly` minestom-extensions and `annotationProcessor` processor; pass `-Aminestom.extension.version=${rootProject.version}` to `compileJava`
- [x] 1.3 Commit as `build(minestom): add minestom-extensions and extension.json generation`

## 2. Extension entry point (test first)

- [x] 2.1 Test: generated `extension.json` has name `Butterfly`, the entrypoint, the project version and no `LuckPerms` dependency (plain file/JSON assertion, no server)
- [x] 2.2 Add `ButterflyExtension` with `@ExtensionInfo(name = "Butterfly")` and no dependencies
- [x] 2.3 Commit as `feat(minestom): add butterfly extension entry point`

## 3. Lifecycle safety (test first)

- [x] 3.1 Tests with a fresh Minestom in-process `Env` per test, explicit `env.tick()`, no sleeps, no system time: inactive plus error log (asserted via captured appender) when LuckPerms is missing; listeners registered when available; `preInitialize()` touches no LuckPerms class
- [x] 3.2 Register listeners on a child `EventNode` of the global handler; defer `LuckPermsProvider` access to `initialize()`; handle absence with an error log
- [x] 3.3 Test: after `terminate()` spawn/chat events are unhandled and created teams are removed; implement removal of node and teams
- [x] 3.4 Test: spawn applies team, colour and prefix; chat is formatted (behaviour identical to library)
- [x] 3.5 Commit as `feat(minestom): make extension lifecycle safe`

## 4. Togglz under the extension classloader

- [x] 4.1 Test: load the extension through an isolated `URLClassLoader` with a different thread context classloader and check the feature manager is found
- [x] 4.2 Fix: set the feature manager explicitly (or bind the context classloader) and read `flags.properties` from `extensions/Butterfly/`, defaulting when absent
- [x] 4.3 Commit as `feat(minestom): resolve togglz inside the extension classloader`

## 5. Docs

- [x] 5.1 README section "Minestom extension": put exactly one Butterfly jar (at least the release containing this change) into `extensions/`; do not also shade or depend on it in the host
- [x] 5.2 Commit as `docs(minestom): document running butterfly as an extension`

## 6. Verify and ship

- [x] 6.1 Run `./gradlew build`; check the shadow jar contains `extension.json`; smoke test on a Titan-like host
- [x] 6.2 Open the pull request titled `feat(minestom): ship butterfly-minestom as a minestom extension`
