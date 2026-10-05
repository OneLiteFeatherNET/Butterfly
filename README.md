# Butterfly
Butterfly is a simple minecraft paper plugin and minestom extension/api that allows you to set scoreboard teams and chat formats from luckperms.

> [!CAUTION]
> This plugin/api is only for internal use and is not intended to be used by the public as a finished product.

## Features
- Set scoreboard teams
- Set chat formats
- Chat formatting (MiniMessage) restricted by permission, see [Chat tag permissions](#chat-tag-permissions)


## Chat tag permissions
Chat messages on Paper and Minestom are parsed as MiniMessage, but a player may only use the tags they have a permission for. Permissions are resolved through LuckPerms, one node per tag type: `butterfly.chat.tag.<type>`. Grant them per rank through LuckPerms group permissions; `butterfly.chat.tag.*` grants every type.

| Permission | Tags |
|------------|------|
| `butterfly.chat.tag.color` | named colours, hex colours, `<color:...>` |
| `butterfly.chat.tag.decoration` | `<bold>`, `<italic>`, `<underlined>`, `<strikethrough>`, `<obfuscated>` and their short forms |
| `butterfly.chat.tag.gradient` | `<gradient>` |
| `butterfly.chat.tag.rainbow` | `<rainbow>` |
| `butterfly.chat.tag.transition` | `<transition>` |
| `butterfly.chat.tag.pride` | `<pride>` |
| `butterfly.chat.tag.shadow` | `<shadow>` |
| `butterfly.chat.tag.font` | `<font>` |
| `butterfly.chat.tag.reset` | `<reset>` |
| `butterfly.chat.tag.newline` | `<newline>` / `<br>` |
| `butterfly.chat.tag.click` | `<click>` |
| `butterfly.chat.tag.hover` | `<hover>` |
| `butterfly.chat.tag.insertion` | `<insert>` |
| `butterfly.chat.tag.keybind` | `<key>` |
| `butterfly.chat.tag.translatable` | `<lang>`, `<tr>`, `<lang_or>` |
| `butterfly.chat.tag.selector` | `<selector>` |
| `butterfly.chat.tag.score` | `<score>` |
| `butterfly.chat.tag.nbt` | `<nbt>` |
| `butterfly.chat.tag.sprite` | `<sprite>` |
| `butterfly.chat.tag.head` | `<head>` |

- A tag the sender has no permission for stays in the message as literal text, exactly as typed.
- The sender's prefix and name keep their MiniMessage formatting regardless of these permissions.
- Every viewer sees the same message; it depends only on the sender's permissions.

### Upgrading to restricted chat tags
Without any `butterfly.chat.tag.*` permission, players lose all chat formatting they had before. To restore the old behaviour, run `lp group default permission set butterfly.chat.tag.* true`.

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
