# Proposal

Ships as: `feat(chat): show the player head before the prefix in chat`

Depends on: `replace-togglz-with-avaje-config` (PR #133) and
`restrict-chat-tags-by-permission` (same chat code path; implement after it).

## Why

Since Minecraft 1.21.9 text components can embed a player's head as an inline
glyph. Showing the sender's head in front of the rank prefix makes chat easier to
scan. Paper (Adventure 5.2) and Minestom both support these object components.

## What Changes

- Chat lines formatted by Butterfly start with the sender's head, followed by a
  space, the prefix and name, `: ` and the message:
  `[head] [Admin] Steve: hello`.
- The head carries the sender's skin textures from their profile, so clients do
  not have to look the skin up themselves.
- New setting `butterfly.chat.head.enabled` (default `true`, Paper and Minestom)
  turns the head off; it is added to both bundled default `config.yaml` files.
- Display name, tab list name and team prefix stay unchanged; the head only
  appears in chat.

## Capabilities

### New Capabilities
- `chat-player-head`: the sender's head in front of the chat line, its skin
  source, and the setting that disables it.

### Modified Capabilities
- `plugin-configuration`: new key `butterfly.chat.head.enabled` in the defaults
  table (spec created by `replace-togglz-with-avaje-config`; archive that change
  first).

## Impact

- Code: `ButterflySettings` and `SettingsFile` defaults in `api`, both
  `default-config.yaml` resources, `bukkit/.../listener/PlayerListener.java`,
  `minestom/.../Butterfly.java` (`playerChat`).
- Clients older than 1.21.9 (via ViaVersion/ViaBackwards) depend on how
  ViaBackwards downgrades object components; verified manually.
- No new dependencies.
