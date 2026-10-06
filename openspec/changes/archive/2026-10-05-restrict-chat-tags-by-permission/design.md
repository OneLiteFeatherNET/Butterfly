# Design

## Context

- Paper `PlayerListener#handleChat` sets a renderer that, **per viewer**,
  serializes the signed message to plain text and parses it with
  `MiniMessage.miniMessage()` (all standard tags).
- Minestom `Butterfly#playerChat` parses `getRawMessage()` with the same default
  instance and returns early (vanilla format) when the sender has no prefix.
- `api` has no Adventure dependency today; LuckPerms access goes through
  `LuckPermsAPI`. Minestom has no permission API of its own, so LuckPerms is the
  only common permission source.
- Minestom tests (`ExtensionBehaviourTest`, `FakeLuckPerms`) already drive chat
  events; the `bukkit` module has no test setup.

## Goals / Non-Goals

**Goals:**
- One platform-neutral component that turns (raw text, permission check) into a
  `Component`, unit-tested in `api`.
- Same behaviour on Paper and Minestom.

**Non-Goals:**
- Per-value permissions (`butterfly.chat.tag.color.red`). The check is built so it
  can be added later without breaking existing nodes (see decision 3).
- Changing the prefix/name part of the chat line or the Minestom early return.
- Keeping components that other plugins put into the Paper message (the current
  plain-text round trip already drops them).

## Decisions

### 1. `ChatMessageParser` in `api`
```
ChatMessageParser.parse(String raw, Predicate<String> hasPermission) -> Component
```
- Builds a `TagResolver` from the tag types whose node passes `hasPermission`
  (table in the spec -> `StandardTags` factories, e.g. `color` ->
  `StandardTags.color()`, `decoration` -> `StandardTags.decorations()`,
  `translatable` -> `translatable()` + `translatableFallback()`, `head` -> the
  `<head>` resolver; task 1.2 confirms that `StandardTags.sequentialHead()` is the
  `<head>` tag).
- Parses with one shared `MiniMessage.builder().tags(TagResolver.empty()).build()`
  instance plus the per-sender resolver. Unknown tags in non-strict mode are kept
  as literal text, which gives "disallowed tags stay literal" for free.
- The tag-type -> resolver table is a single `enum ChatTagType` so the README list,
  the spec table and the code have one source.

*Alternative:* `MiniMessage.stripTags`/escape for disallowed tags. Rejected:
stripping alters text (`a <b> c`), escaping adds backslashes viewers would see.

### 2. Permission check via LuckPerms, not the platform
`LuckPermsAPI` gets `hasPermission(UUID, String)` using the user's cached
permission data with the user's contextual query options. Paper's
`Player#hasPermission` would route to LuckPerms anyway; using LuckPerms directly
keeps one code path and lets the Minestom `FakeLuckPerms` drive tests. LuckPerms
cached data is safe to read from Paper's async chat thread.

### 3. Node shape `butterfly.chat.tag.<type>`
Lower-case snake_case type names (`translatable`, `shadow`). A later per-value extension
checks `has(type) || has(type + "." + value)`, so nodes granted now keep working.

### 4. Parse once per message
Paper: parse in `handleChat` before setting the renderer and capture the result;
the renderer only composes `displayName + ": " + parsed`. Minestom already handles
the event once.

### 5. Dependencies
`api` gets `compileOnly` Adventure API + MiniMessage (versions from the mycelium
BOM, matching Paper's and Minestom's runtime) and the same as
`testImplementation`.

## Risks / Trade-offs

- [Servers upgrade and everyone loses formatting] -> BREAKING note with the one-line
  migration (grant `butterfly.chat.tag.*` to `default`), README section.
- [`StandardTags` gains new tags in a future Adventure version] -> they are not in
  `ChatTagType`, so they stay literal until added; safe default.
- [LuckPerms user not loaded (e.g. very early chat)] -> treat as no permission:
  text stays literal, nothing breaks.

## Migration Plan

Release as `feat(chat)!` with `BREAKING CHANGE:` footer naming the migration node.
Rollback: previous jar restores full-tag parsing.
