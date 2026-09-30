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
- Feature flags are read from `extensions/Butterfly/flags.properties` (for example `TEAM_COLLISION=true`); defaults apply when the file is absent.
- Library users keep calling `Butterfly.create().load()` and `terminate()`.
