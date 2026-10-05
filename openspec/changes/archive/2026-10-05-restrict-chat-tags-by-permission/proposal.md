# Proposal

Ships as: `feat(chat)!: restrict minimessage tags in chat by permission`

Depends on: `replace-togglz-with-avaje-config` (PR #133) being merged.

## Why

Both platforms parse every chat message with the full default MiniMessage
instance (`PlayerListener#handleChat` on Paper, `Butterfly#playerChat` on
Minestom). Any player can therefore send click events that run commands, hover
texts, selectors, NBT lookups, fonts, sprites and player heads. Servers want to
grant formatting per rank instead, and LuckPerms already models ranks.

## What Changes

- Chat messages are parsed with only the MiniMessage tags the sender has a
  permission for: `butterfly.chat.tag.<type>`, one node per tag type (for example
  `color`, `decoration`, `gradient`, `click`, `hover`, `head`). Granting per rank
  happens through LuckPerms group permissions; `butterfly.chat.tag.*` grants all.
- Tags the sender may not use stay in the message as literal text.
- Permissions are resolved through LuckPerms, so Paper and Minestom behave the
  same.
- The message is parsed once per chat message instead of once per viewer (Paper).
- **BREAKING**: without permissions, players lose all formatting they have today.
  Granting `butterfly.chat.tag.*` to the `default` group restores the old
  behaviour.

## Capabilities

### New Capabilities
- `chat-tag-permissions`: which MiniMessage tags a chat sender may use, how
  permissions map to tag types, and what happens to tags that are not allowed.

### Modified Capabilities
<!-- none: minestom-extension's "Chat format" (prefix followed by the message) still holds -->

## Impact

- Code: `bukkit/.../listener/PlayerListener.java`,
  `minestom/.../Butterfly.java` (`playerChat`), new platform-neutral code in `api`.
- Build: `api` needs Adventure MiniMessage as `compileOnly` (provided by Paper and
  Minestom at runtime).
- Operators: must grant `butterfly.chat.tag.<type>` nodes per group; document the
  node list in the README.
- Follow-up `add-player-head-to-chat` builds its chat line on the same code path.
