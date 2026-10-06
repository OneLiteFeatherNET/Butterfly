# minestom-extension Specification

## Purpose
Defines how Butterfly runs as a Minestom extension loaded from the `extensions/` directory, while remaining usable as a library.

## Requirements

### Requirement: Loadable extension descriptor
The `butterfly-minestom` jar SHALL contain a generated `extension.json` with name `Butterfly`, the `ButterflyExtension` entrypoint and the project version, and SHALL NOT declare a dependency on `LuckPerms`.

#### Scenario: Jar placed in extensions directory
- **WHEN** the jar is placed in `extensions/` and the server starts
- **THEN** the extension manager loads Butterfly without a "Missing extension.json" error

#### Scenario: No LuckPerms extension present
- **WHEN** no extension named LuckPerms exists and LuckPerms runs in-process
- **THEN** Butterfly is still loaded

### Requirement: Initialization after LuckPerms
The extension SHALL access `LuckPermsProvider` only in `initialize()` or later and SHALL NOT access it in `preInitialize()`.

#### Scenario: LuckPerms available at initialize
- **WHEN** `initialize()` runs and LuckPerms is registered
- **THEN** the extension becomes active and registers its listeners

#### Scenario: Pre-initialization
- **WHEN** `preInitialize()` runs before LuckPerms is up
- **THEN** no LuckPerms class is touched and no error occurs

### Requirement: Inactive without LuckPerms
If LuckPerms is unavailable during `initialize()`, the extension SHALL log an error naming the cause and remain inactive without throwing out of the lifecycle method.

#### Scenario: LuckPerms missing
- **WHEN** `initialize()` runs and LuckPerms is not available
- **THEN** an error is logged, no listeners are registered and the server keeps running

### Requirement: Team and prefix on spawn
On player spawn the extension SHALL place the player in a Minestom team derived from their LuckPerms primary group (name formatted with the configured sort format `butterfly.teams.sort-format`, default `%04d`, + group for tab list ordering) and apply the team colour and prefix as nametag prefix, identical to the library behaviour.

#### Scenario: Player spawns
- **WHEN** a player with a LuckPerms group spawns
- **THEN** the player is a member of the group's team with the configured colour and prefix

#### Scenario: Custom sort format
- **WHEN** `butterfly.teams.sort-format` is `%02d` and a player whose primary group has sort id 5 spawns
- **THEN** the player's team name starts with `05`

### Requirement: Chat format
The extension SHALL format chat messages as the MiniMessage group prefix followed by the message, identical to the library behaviour.

#### Scenario: Player chats
- **WHEN** a player sends a chat message
- **THEN** the rendered message contains the group prefix and the original message

### Requirement: Clean termination
On `terminate()` the extension SHALL remove the event node it registered and unregister the teams it created.

#### Scenario: Extension terminated
- **WHEN** `terminate()` runs
- **THEN** later spawn and chat events are not handled by Butterfly and the created teams no longer exist

### Requirement: Library compatibility
The `butterfly-minestom` artifact SHALL remain usable as a library through `Butterfly.create().load()` and `terminate()` without the extension system.

#### Scenario: Library usage
- **WHEN** a host application depends on the jar and calls `Butterfly.create().load()`
- **THEN** listeners are registered as before
