# Spec Delta

## MODIFIED Requirements

### Requirement: Team and prefix on spawn
On player spawn the extension SHALL place the player in a Minestom team derived from their LuckPerms primary group (name formatted with the configured sort format `butterfly.teams.sort-format`, default `%04d`, + group for tab list ordering) and apply the team colour and prefix as nametag prefix, identical to the library behaviour.

#### Scenario: Player spawns
- **WHEN** a player with a LuckPerms group spawns
- **THEN** the player is a member of the group's team with the configured colour and prefix

#### Scenario: Custom sort format
- **WHEN** `butterfly.teams.sort-format` is `%02d` and a player whose primary group has sort id 5 spawns
- **THEN** the player's team name starts with `05`

## REMOVED Requirements

### Requirement: Feature flags under the extension classloader
**Reason**: Togglz is replaced by avaje-config; the extension no longer reads `flags.properties`.
**Migration**: Set `butterfly.teams.collision` in `extensions/Butterfly/config.yaml` (see capability `plugin-configuration`) and delete `extensions/Butterfly/flags.properties`.
