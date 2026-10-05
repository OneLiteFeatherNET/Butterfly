# Butterfly
Butterfly is a simple minecraft paper plugin and minestom extension/api that allows you to set scoreboard teams and chat formats from luckperms.

> [!CAUTION]
> This plugin/api is only for internal use and is not intended to be used by the public as a finished product.

## Features
- Set scoreboard teams
- Set chat formats
- Chat color support


## Minestom extension
`butterfly-minestom.jar` is a Minestom extension (name `Butterfly`) and still works as a library.

- Put exactly one Butterfly jar into the server's `extensions/` directory (at least the release containing the extension entry point). Do not also shade it into the host or depend on it there, two copies conflict.
- LuckPerms must run inside the host. Butterfly declares no extension dependency and only accesses LuckPerms in `initialize()`. If LuckPerms is not available it logs an error and stays inactive.
- Settings are read from `extensions/Butterfly/config.yaml`, see [Configuration](#configuration).
- Library users keep calling `Butterfly.create().load()` and `terminate()`. `Butterfly.create()` uses the defaults overridden only by system properties and never touches the file system; `Butterfly.create(ButterflySettings)` takes the settings from the host, for example `Butterfly.create(new ButterflySettings("%02d", true)).load()`.

## Configuration
Butterfly reads `config.yaml` from its own data folder and writes it with every default on first start; an existing file is never overwritten.

| Platform | File |
|----------|------|
| Paper | `plugins/Butterfly/config.yaml` |
| Minestom extension | `extensions/Butterfly/config.yaml` |

| Key | Platform | Default | Description |
|-----|----------|---------|-------------|
| `butterfly.teams.sort-format` | Paper, Minestom | `%04d` | `String.format` pattern for the numeric team-name prefix that sorts the tab list |
| `butterfly.teams.collision` | Minestom | `false` | `true` makes players in the same team push each other |

```yaml
butterfly:
  teams:
    sort-format: "%04d"
    collision: false
```

- An unusable value falls back to its default and logs a warning naming the key and the value.
- A JVM system property with the same name as a key (for example `-Dbutterfly.teams.collision=true`) overrides the file. The older `-Dbutterfly.format=%03d` still works as an alias for `butterfly.teams.sort-format`; if both are set, `butterfly.teams.sort-format` wins.
- The file is `config.yaml`, not Paper's usual `config.yml`.

### Upgrading from `flags.properties`
`flags.properties` (Paper: server root, Minestom: `extensions/Butterfly/`) is no longer read. Delete it; Butterfly logs a warning at start for as long as it exists and never modifies it. Minestom servers that had `TEAM_COLLISION=true` must set `butterfly.teams.collision: true` in `extensions/Butterfly/config.yaml`.
