# Design

## Context

- After `restrict-chat-tags-by-permission`, both platforms build the message part
  with `ChatMessageParser` (api) once per chat message. Paper composes
  `displayName + ": " + message` in the `AsyncChatEvent` renderer; Minestom builds
  `prefix + name + ": " + message` in `Butterfly#playerChat` and returns early
  (vanilla format) when the sender has no prefix.
- Adventure 5.2 provides `Component.object(...)` with `PlayerHeadObjectContents`
  (UUID, name, profile properties). Minestom 26.2 encodes `ObjectComponent`
  (`ComponentCodecs`), Paper 26.x natively.
- Skin sources: Paper `Player#getPlayerProfile().getProperties()` (`textures`
  with value + signature); Minestom `Player#getSkin()` (`textures`, `signature`,
  may be `null`).
- Settings live in `ButterflySettings` and the namespaced `default-config.yaml`
  resources (from `replace-togglz-with-avaje-config`).

## Goals / Non-Goals

**Goals:**
- One platform-neutral builder for the head and the chat line, unit-tested in `api`.
- No client-side skin lookups for online-mode players.

**Non-Goals:**
- Heads in tab list, nametags, or other messages (join/quit, death).
- Permission-gating the head per rank (setting is global).
- Changing the Minestom early return for players without prefix.

## Decisions

### 1. `ChatLine` in `api`
```
ChatLine.compose(@Nullable Component head, Component name, Component message) -> Component
PlayerHeads.of(UUID id, String name, @Nullable String texture, @Nullable String signature) -> Component
```
`compose` emits `head + " "` only when `head` is non-null, then
`name + ": " + message`. Platforms pass `null` when
`settings.chatHeadEnabled()` is false. Pure functions -> fast unit tests without a
server.

*Alternative:* put the head into the display name. Rejected in exploration: the tab
list already shows heads (duplicate) and other plugins reading the display name
would inherit it.

### 2. Head built once per message
Paper: build the head in `handleChat` next to the parsed message and capture it in
the renderer. Minestom: build in `playerChat`.

### 3. Setting `butterfly.chat.head.enabled`
Added to `ButterflySettings` (default `true`, invalid -> default + warning, same
rules as `butterfly.teams.collision`) and to both bundled `default-config.yaml`
files. Existing data-folder files are never rewritten, so servers that already
have a `config.yaml` get the default from code.

### 4. Space after the head
A plain `Component.space()` keeps the glyph from touching the prefix's first
character; no configurable separator.

## Risks / Trade-offs

- [Clients < 1.21.9 via ViaBackwards: unknown what the downgrade produces (nothing,
  placeholder, or fallback text)] -> manual check with an old client (task 4.2);
  if the result is ugly, a follow-up can make the head per-client-version, the
  setting allows turning it off meanwhile.
- [Offline-mode servers have no textures; clients may resolve by name or show a
  default skin] -> accepted, spec requires UUID + name only in that case.
- [The leading space shifts existing chat layout] -> visible change, documented.

## Migration Plan

Additive `feat(chat)`. Default on; operators set `butterfly.chat.head.enabled:
false` to keep the old look. Rollback: previous jar.
