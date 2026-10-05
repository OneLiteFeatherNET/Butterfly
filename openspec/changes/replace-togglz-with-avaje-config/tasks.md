# Tasks

Branch from the up-to-date `origin/main`. Commit type for all implementation
commits: `refactor(config)`; the breaking commit carries `!` and a
`BREAKING CHANGE:` footer.

## 1. Dependencies and spike

- [x] 1.1 Add `avaje-config` (5.2) to the version catalog in `settings.gradle.kts` and as `implementation` in `api/build.gradle.kts`; verify `./gradlew :api:dependencies --configuration runtimeClasspath` lists `io.avaje:avaje-config:5.2`
- [x] 1.2 Spike: find the `Configuration.builder()` calls that load exactly one explicit YAML file plus system properties without scanning classpath or working directory, and check whether building needs the context classloader switched; record the result under design.md decision 1 and prove it with a `@TempDir` test that places a decoy `application.yaml` and asserts it is not read

## 2. Settings in `api`

- [x] 2.1 Write failing unit tests for `ButterflySettings.from(Configuration, Logger)` covering the requirements "Missing keys fall back to defaults", "Invalid values fall back to defaults with a warning" and "System properties override file values" (configuration built with `Configuration.builder().put(...)`, warnings asserted through a captured logger, no `System.setProperty`); verify they fail with `./gradlew :api:test`
- [x] 2.2 Implement the `ButterflySettings` record, `defaults()` and `from(...)`; verify `./gradlew :api:test` passes
- [ ] 2.3 Write failing `@TempDir` tests for `SettingsFile.load(...)`: default file written when absent, existing file untouched, folder created, unwritable folder -> defaults + warning, one `flags.properties` warning naming `butterfly.teams.collision` with the legacy file left unchanged, decoy `config.yaml` in another directory ignored; then implement until `./gradlew :api:test` passes

## 3. Paper module

- [ ] 3.1 Add `bukkit/src/main/resources/config.yaml` with the Paper defaults from design.md decision 5; verify `unzip -l bukkit/build/libs/butterfly-paper-*.jar | grep config.yaml`
- [ ] 3.2 In `Butterfly#onEnable` load settings via `SettingsFile.load(getDataPath(), ...)` with legacy path `flags.properties` in the working directory, and pass them into `BukkitLuckPermsService` by constructor replacing the static `FORMAT`; verify `./gradlew :bukkit:build` succeeds and `grep -rn "butterfly.format" bukkit/src/main` returns nothing
- [ ] 3.3 Delete `bukkit/.../feature/`, `bukkit/.../utils/ThreadHelper.java` and `bukkit/src/main/resources/META-INF/services/org.togglz.core.spi.FeatureManagerProvider`, drop `libs.togglz` from `bukkit/build.gradle.kts`; verify `grep -rni togglz bukkit/src bukkit/build.gradle.kts` returns nothing and `./gradlew :bukkit:build` succeeds

## 4. Minestom module

- [ ] 4.1 Add `minestom/src/main/resources/config.yaml` with the Minestom defaults from design.md decision 5; verify it is in the shaded jar via `unzip -l`
- [ ] 4.2 Write failing tests: extension started with a `@TempDir` data directory whose `config.yaml` sets `butterfly.teams.collision: true` gives the spawned player's team collision `ALWAYS`; a data directory with only `flags.properties` (`TEAM_COLLISION=true`) gives `NEVER` and one warning; `Butterfly.create(settings)` with a custom sort format names teams accordingly; `Butterfly.create()` creates no file in the working directory. Fresh `Env` per test, ticks driven explicitly; verify they fail with `./gradlew :minestom:test`
- [ ] 4.3 Pass `ButterflySettings` into `MinestomLuckPermsService` by constructor (sort format, collision), add `Butterfly.create(ButterflySettings)` and an internal `create(parent, settings)`, make `Butterfly.create()` use defaults + system properties, and replace the `ButterflyFeatures.configure(...)` block in `ButterflyLifecycle.start()` with `SettingsFile.load(dataDirectory, ...)`; verify `./gradlew :minestom:test` passes including `ExtensionBehaviourTest.collisionIsOffByDefault`
- [ ] 4.4 Delete `minestom/.../feature/`, `FeatureFlagsTest`, `ExtensionFlagsFileTest` (their still-relevant cases, such as "missing data directory is created", are covered by 2.3/4.2) and the Togglz SPI file if present, drop `libs.togglz` from `minestom/build.gradle.kts`; verify `grep -rni togglz minestom/src minestom/build.gradle.kts` returns nothing and `./gradlew :minestom:test` passes

## 5. Packaging and docs

- [ ] 5.1 Relocate `io.avaje.config` to `net.onelitefeather.butterfly.libs.avaje.config` in both `shadowJar` blocks (keep `mergeServiceFiles()`); verify with `unzip -l` that no `io/avaje/` entries remain in either shaded jar and that `./gradlew :minestom:smokeTest` (shaded jar via the extension manager) passes
- [ ] 5.2 Remove the `togglz` entry from the version catalog; verify `grep -rni togglz settings.gradle.kts` returns nothing and `./gradlew build` succeeds
- [ ] 5.3 Replace the feature-flag lines in `README.md` with a configuration section: both `config.yaml` locations, every key with its default from the spec table, `-Dbutterfly.format` legacy override, `Butterfly.create(ButterflySettings)` for library users, and the note to delete `flags.properties`; verify every key from the spec table appears in the README
- [ ] 5.4 Manual Paper smoke test via `./gradlew :bukkit:runServer` with LuckPerms: `plugins/Butterfly/config.yaml` is created on first start and unchanged on the second; with `sort-format: "%02d"` and `/updateteams` team names have a two-digit prefix; with `-Dbutterfly.format=%03d` and no key in the file, a three-digit prefix (report results in the PR description)

## 6. Pull request

- [ ] 6.1 Open the pull request titled `refactor(config)!: replace togglz with avaje-config` with a `BREAKING CHANGE:` section (`flags.properties` no longer read, new data-folder `config.yaml`, Minestom collision via `butterfly.teams.collision`); verify `gh pr view` shows the title and CI is green
