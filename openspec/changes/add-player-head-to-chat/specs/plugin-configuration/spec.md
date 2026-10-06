# Spec Delta

## MODIFIED Requirements

### Requirement: Missing keys fall back to defaults
Every setting SHALL have a documented default that applies when the key is absent
from all configuration sources.

| Key | Platform | Default |
|-----|----------|---------|
| `butterfly.teams.sort-format` | Paper, Minestom | `%04d` |
| `butterfly.teams.collision` | Minestom | `false` |
| `butterfly.chat.head.enabled` | Paper, Minestom | `true` |

#### Scenario: Empty settings file
- **WHEN** the data-folder `config.yaml` exists but is empty
- **THEN** every setting takes its default value

#### Scenario: Collision stays off by default
- **WHEN** no source sets `butterfly.teams.collision`
- **AND** a player spawns on Minestom
- **THEN** the player's team has collision rule `NEVER`

#### Scenario: Existing settings file without the head key
- **WHEN** a `config.yaml` written by an earlier version has no `butterfly.chat.head.enabled`
- **THEN** the head is shown in chat
