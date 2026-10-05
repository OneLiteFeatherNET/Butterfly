# Design

## Context

- Paper: Togglz is wired through `SingletonFeatureManagerProvider` (ServiceLoader
  SPI) and reads `new File("flags.properties")`, i.e. the server root. The Paper
  `ButterflyFeatures.TEAM_COLLISION` is never read.
- Minestom runs as an extension (`ButterflyExtension` -> `ButterflyLifecycle`) or
  as a library (`Butterfly.create().load()`). `ButterflyLifecycle.start()` creates
  the data directory and calls `ButterflyFeatures.configure(dataDir/flags.properties)`;
  Togglz needed explicit configuration and context-classloader switching because
  the extension classloader is not the thread context classloader.
  `TEAM_COLLISION` has no `@EnabledByDefault`, so collision is `NEVER` unless set.
- `butterfly.format` is read with `System.getProperty` in a `static final` field in
  both services, so it cannot be varied per test.
- `api` is shaded into both platform jars. Togglz is shaded without relocation.
- Existing Minestom tests use `minestom-testing` (`Env`, `@TempDir` data dirs) and a
  separate `smokeTest` source set that loads the shaded jar through the real
  extension manager.

## Goals / Non-Goals

**Goals:**
- One immutable settings object, built once at startup, passed into the services
  by constructor. No static configuration lookups.
- Settings parsing and validation testable as plain unit tests (no server, no real
  filesystem besides `@TempDir`).
- avaje-config never visible to, or clashing with, other plugins, extensions or
  the host.

**Non-Goals:**
- Hot reload of settings.
- Implementing team collision on Paper (would be a `feat`).
- The chat settings (tag permissions, player head). They land in follow-up
  changes and only add keys to the object introduced here.

## Decisions

### 1. Own `Configuration` instance, not the static `Config`
The static `io.avaje.config.Config` loads `application.yaml/properties` from the
classpath and the working directory automatically, and is a singleton per
classloader. We build a dedicated instance with `Configuration.builder()` that
loads only the data-folder `config.yaml` plus system properties.

*Alternative:* static `Config` with `load.properties` pointing at the data folder.
Rejected: it still scans the server root first.

*To verify first (task 1.2):* which builder method loads an explicit file and how
to switch off the default resource/working-directory loading, and whether the
builder discovers parsers via ServiceLoader (classloader issue, see Risks). If the
builder cannot exclude default sources, read the YAML with avaje's parser and feed
the entries in with `put(...)`.

*Spike result (task 1.2, avaje-config 5.2):* `Configuration.builder()` alone loads
nothing from the classpath or the working directory; standard resource loading
(`application.yaml` etc.) only happens after `includeResourceLoading()`, which we
never call. `Configuration.builder().load(File)` reads exactly that file (parser
chosen by extension; the built-in simple YAML parser is used because SnakeYAML is
not on the classpath; nested keys are flattened with dots, quoted values such as
`"%04d"` work). The builder needs no `put(...)` fallback.
`put(...)`/`load(...)` entries and keys that are absent entirely are overridden by
a same-named JVM system property (then by an environment variable, avaje's own
rule), so the legacy `butterfly.format` property is visible through the same
`Configuration`. The only ServiceLoader use is `ServiceLoader.load(ConfigExtension.class)`
for optional extensions (custom parsers, log, sources) which Butterfly does not
use; with a foreign thread context classloader it finds none and avaje falls back
to its built-in defaults, so no context-classloader switch is needed (confirmed by
the shaded-jar `smokeTest`, task 5.1). Proven by `AvajeConfigurationSpikeTest`
(a decoy `application.yaml` on the classpath and next to the file is not read).

### 2. `ButterflySettings` record and loader in `api`
- `ButterflySettings` record (`sortFormat`, `teamCollision`) with `defaults()`.
- `ButterflySettings.from(Configuration, Logger)`: defaults, legacy
  `butterfly.format` alias, validation (invalid -> default + warning).
- `SettingsFile.load(Path dataFolder, String defaultResource, Logger)`: creates the
  folder, writes the bundled default `config.yaml` if absent, builds the
  `Configuration`, warns once about `flags.properties` at a given legacy path, and
  falls back to defaults + warning when the folder cannot be written.

- `SettingsFile.fromSystemProperties(Logger)`: defaults + system properties, no file
  I/O (Minestom library path).
- Precedence: system property `butterfly.teams.sort-format` > system property
  `butterfly.format` > `config.yaml` > default. `SettingsFile` takes the system
  properties as an injectable map (tests never call `System.setProperty`); the legacy
  property is translated onto the new key after the file is loaded, because the
  generated default file always contains the new key and would otherwise shadow
  `-Dbutterfly.format`.

Both platforms share this code, so the Paper and Minestom paths differ only in the
data folder, the legacy `flags.properties` location and the default resource.

*Alternative:* each platform reads `Configuration` itself. Rejected: duplicates
defaults, validation and file handling.

### 3. Entry points
- Paper `Butterfly#onEnable`: `SettingsFile.load(getDataPath(), ...)`, legacy path
  `flags.properties` in the working directory; pass settings to
  `BukkitLuckPermsService`.
- Minestom extension `ButterflyLifecycle.start()`: replaces the
  `ButterflyFeatures.configure(...)` block with `SettingsFile.load(dataDirectory, ...)`,
  legacy path `dataDirectory/flags.properties`; then `Butterfly.create(parent, settings)`.
- Minestom library: `Butterfly.create()` = defaults + system properties, no file
  I/O; new public `Butterfly.create(ButterflySettings)`.

The Minestom public API takes `ButterflySettings`, not avaje `Configuration`:
avaje is relocated inside the shaded jar, so a signature with
`io.avaje.config.Configuration` would expose a type callers cannot construct.

### 4. Relocate avaje in both shadow jars
`io.avaje.config` -> `net.onelitefeather.butterfly.libs.avaje.config` in `bukkit`
and `minestom` `shadowJar`, keeping `mergeServiceFiles()` so avaje's ServiceLoader
entries follow the relocation. The existing Minestom `smokeTest` loads the shaded
jar through the extension manager and therefore covers relocation + classloading.

### 5. Default files as bundled resources
Each platform ships its own commented `config.yaml` resource with only the keys it
supports. Keys are namespaced under `butterfly:` so system-property overrides
cannot collide with other software in the same JVM.

```yaml
# Paper
butterfly:
  teams:
    # String.format pattern for the numeric team-name prefix (tab sorting)
    sort-format: "%04d"
```
```yaml
# Minestom
butterfly:
  teams:
    sort-format: "%04d"
    # true = players in the same team push each other
    collision: false
```

### 6. Remove Togglz completely
Delete both `feature` packages, `ThreadHelper`, the SPI files and `libs.togglz`.
Replace `FeatureFlagsTest` and `ExtensionFlagsFileTest` (Togglz-specific) with
tests for `ButterflySettings`/`SettingsFile` and an extension test that reads
`config.yaml` from the `@TempDir` data directory.

## Risks / Trade-offs

- [avaje may discover parsers/sources via ServiceLoader with the thread context
  classloader, which is the host's under the extension classloader and Paper's
  plugin loader] -> spike in task 1.2; if needed, switch the context classloader
  around the single build call (the pattern `SingletonFeatureManagerProvider` uses
  today). The Minestom `smokeTest` catches regressions.
- [Togglz created `flags.properties` automatically, so most servers have one] ->
  the warning fires every start until the operator deletes the file; acceptable and
  documented, the file is never touched by Butterfly.
- [Minestom servers with `TEAM_COLLISION=true` silently lose collision] -> the
  warning names the replacement key; `BREAKING CHANGE:` footer and README.
- [`config.yaml` vs Paper's usual `config.yml`] -> accepted; avaje picks the
  parser by extension. Documented in the README.

## Migration Plan

1. Release with `refactor(config)!` and a `BREAKING CHANGE:` footer: `flags.properties`
   no longer read, new data-folder `config.yaml`, collision via
   `butterfly.teams.collision`.
2. Rollback: redeploy the previous jar; it ignores `config.yaml` and reads the
   untouched `flags.properties` again.
