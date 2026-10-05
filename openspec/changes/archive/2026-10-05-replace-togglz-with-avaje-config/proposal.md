# Proposal

Ships as: `refactor(config)!: replace togglz with avaje-config`

## Why

Butterfly configures itself through Togglz feature flags (`flags.properties`: in
the server root on Paper, in `extensions/Butterfly/` on Minestom) plus a
`butterfly.format` system property read in two services. The upcoming chat changes
(MiniMessage tag permissions, player heads in chat) need real configuration keys,
and Togglz is a feature-flag framework whose user/activation-strategy machinery
Butterfly never uses, while its ServiceLoader lookup needed class-loader
workarounds for the Minestom extension. Replacing it with avaje-config now gives
one place for all settings before more keys are added.

## What Changes

- Remove Togglz (`ButterflyFeatures`, `SingletonFeatureManagerProvider`,
  `ThreadHelper`, the `META-INF/services` registrations and the `libs.togglz`
  dependency) from both the `bukkit` and `minestom` modules.
- Add avaje-config as the configuration source. Settings are read once into an
  immutable `ButterflySettings` object and passed to the services instead of being
  looked up through static singletons.
- **BREAKING** (Paper): settings move from `flags.properties` in the server root to
  `plugins/Butterfly/config.yaml`, written with defaults on first start.
- **BREAKING** (Minestom extension): settings move from
  `extensions/Butterfly/flags.properties` to `extensions/Butterfly/config.yaml`,
  written with defaults on first start. The flag `TEAM_COLLISION` becomes
  `butterfly.teams.collision`, default `false` as today.
- **BREAKING** (Paper): the Paper-side `TEAM_COLLISION` flag is removed. It was
  declared but never read, so no runtime behaviour changes.
- Minestom as a library: `Butterfly.create()` keeps working with defaults plus
  system properties; a new overload `Butterfly.create(ButterflySettings)` lets the
  host supply values.
- `flags.properties` is no longer read on either platform; if one is found, a
  single warning names the replacement setting.
- `butterfly.format` becomes the key `butterfly.teams.sort-format`. The system
  property `-Dbutterfly.format` keeps overriding it, so existing launch scripts
  keep working.
- Shade and relocate avaje-config so it cannot clash with other plugins,
  extensions or the host.

## Capabilities

### New Capabilities
- `plugin-configuration`: where Butterfly reads its settings from on each
  platform, which keys exist, their defaults, and how overrides take precedence.

### Modified Capabilities
- `minestom-extension`: the Togglz requirement "Feature flags under the extension
  classloader" is removed (replaced by `plugin-configuration`); "Team and prefix on
  spawn" uses the configured sort format instead of a fixed `%04d`.

## Impact

- Code: `bukkit/.../feature/*`, `bukkit/.../utils/ThreadHelper.java`,
  `minestom/.../feature/*`, `BukkitLuckPermsService`, `MinestomLuckPermsService`,
  `bukkit/.../Butterfly`, `minestom/.../Butterfly`, `ButterflyLifecycle`, both
  `META-INF/services` Togglz files.
- Tests: `FeatureFlagsTest` and `ExtensionFlagsFileTest` are Togglz-specific and
  are replaced by settings/config-file tests; `ExtensionBehaviourTest` keeps its
  collision-off-by-default expectation.
- Build: `settings.gradle.kts` version catalog (drop `togglz`, add
  `avaje-config`), `api`, `bukkit` and `minestom` build files (relocation in
  `shadowJar`).
- Operators: delete `flags.properties`; Minestom servers that set
  `TEAM_COLLISION=true` must set `butterfly.teams.collision: true` in
  `extensions/Butterfly/config.yaml`.
- Docs: README sections on feature flags.
- Unblocks the follow-up changes `feat(chat)!` (tag permissions) and `feat(chat)`
  (player head in chat).
