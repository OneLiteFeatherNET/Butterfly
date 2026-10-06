# plugin-configuration Specification

## Purpose
Defines where Butterfly reads its settings from on Paper and Minestom, which
settings exist with which defaults, and how overrides take precedence, so
operators have one predictable place to configure Butterfly.

## Requirements

### Requirement: Settings are read from the data folder
Butterfly SHALL read its settings from `config.yaml` inside its own data folder:
`plugins/Butterfly/config.yaml` on Paper and `extensions/Butterfly/config.yaml`
when running as a Minestom extension. It MUST NOT read settings files from the
server's working directory.

#### Scenario: Paper settings file is applied
- **WHEN** `plugins/Butterfly/config.yaml` sets `butterfly.teams.sort-format` to `%03d`
- **AND** the plugin is enabled
- **THEN** team names are built with a three-digit sort prefix

#### Scenario: Minestom extension settings file is applied
- **WHEN** `extensions/Butterfly/config.yaml` sets `butterfly.teams.collision` to `true`
- **AND** the extension is initialized and a player spawns
- **THEN** the player's team has collision rule `ALWAYS`

#### Scenario: Settings file in the server root is ignored
- **WHEN** a file named `application.yaml` or `config.yaml` in the server's working directory sets `butterfly.teams.sort-format`
- **AND** the data-folder `config.yaml` does not set it
- **THEN** the default sort format `%04d` is used

### Requirement: Default settings file is created on first start
When the data-folder `config.yaml` does not exist at start, Butterfly SHALL create
the data folder if needed and write `config.yaml` with every key supported on that
platform set to its default value. An existing file MUST NOT be overwritten or
modified.

#### Scenario: First start writes defaults
- **WHEN** Butterfly starts and its data-folder `config.yaml` is absent
- **THEN** the file exists afterwards and contains `butterfly.teams.sort-format` with value `%04d`

#### Scenario: Existing file is kept
- **WHEN** Butterfly starts and its data-folder `config.yaml` already exists with custom values
- **THEN** the file content is unchanged afterwards

#### Scenario: Data folder cannot be written
- **WHEN** the data folder cannot be created or written
- **THEN** a warning naming the folder is logged
- **AND** Butterfly starts with default settings

### Requirement: Missing keys fall back to defaults
Every setting SHALL have a documented default that applies when the key is absent
from all configuration sources.

| Key | Platform | Default |
|-----|----------|---------|
| `butterfly.teams.sort-format` | Paper, Minestom | `%04d` |
| `butterfly.teams.collision` | Minestom | `false` |

#### Scenario: Empty settings file
- **WHEN** the data-folder `config.yaml` exists but is empty
- **THEN** every setting takes its default value

#### Scenario: Collision stays off by default
- **WHEN** no source sets `butterfly.teams.collision`
- **AND** a player spawns on Minestom
- **THEN** the player's team has collision rule `NEVER`

### Requirement: Invalid values fall back to defaults with a warning
When a setting has a value that cannot be used (for example a sort format that
cannot format a single integer, or a collision value that is not a boolean),
Butterfly SHALL use that setting's default and log a warning naming the key and
the rejected value. Startup MUST continue.

#### Scenario: Unusable sort format
- **WHEN** `butterfly.teams.sort-format` is set to `%s%s`
- **THEN** the default `%04d` is used
- **AND** a warning naming `butterfly.teams.sort-format` and `%s%s` is logged

#### Scenario: Non-boolean collision value
- **WHEN** `butterfly.teams.collision` is set to `sometimes`
- **THEN** the default `false` is used
- **AND** a warning naming `butterfly.teams.collision` and `sometimes` is logged

### Requirement: System properties override file values
A JVM system property with the same name as a setting key SHALL take precedence
over the value from the settings file. The legacy system property
`butterfly.format` SHALL continue to override `butterfly.teams.sort-format`; when
both are set, `butterfly.teams.sort-format` wins.

#### Scenario: Legacy format property still works
- **WHEN** the server is started with `-Dbutterfly.format=%02d`
- **AND** no other source sets `butterfly.teams.sort-format`
- **THEN** team names are built with a two-digit sort prefix

#### Scenario: New property beats legacy property
- **WHEN** the server is started with `-Dbutterfly.format=%02d` and `-Dbutterfly.teams.sort-format=%05d`
- **THEN** team names are built with a five-digit sort prefix

### Requirement: Legacy flags file is reported, not read
Butterfly MUST NOT read `flags.properties`. When a `flags.properties` file exists
at the location the previous version used (the working directory on Paper, the
extension data folder on Minestom), Butterfly SHALL log a single warning at start
that the file is no longer used and name `butterfly.teams.collision` as its
replacement. The file MUST NOT be modified or deleted.

#### Scenario: Old flags file present
- **WHEN** `extensions/Butterfly/flags.properties` with `TEAM_COLLISION=true` exists
- **AND** the Minestom extension is initialized and a player spawns
- **THEN** one warning mentions `flags.properties` and `butterfly.teams.collision`
- **AND** the player's team has collision rule `NEVER`

### Requirement: Minestom library takes settings from the host
When Butterfly is used as a Minestom library (not loaded as an extension), it
SHALL accept its settings from the host at creation time. When the host passes no
settings, Butterfly SHALL use the defaults overridden only by JVM system
properties, and MUST NOT read or write any file.

#### Scenario: Host enables collision
- **WHEN** the host creates Butterfly with settings where `butterfly.teams.collision` is `true`
- **AND** a player spawns
- **THEN** the player's team has collision rule `ALWAYS`

#### Scenario: No settings passed
- **WHEN** the host creates Butterfly without settings
- **AND** a player spawns
- **THEN** the player's team has collision rule `NEVER`
- **AND** no file is created in the working directory
