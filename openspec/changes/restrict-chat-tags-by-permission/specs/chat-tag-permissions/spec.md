# Spec Delta

## Purpose

Controls which MiniMessage formatting tags a player may use in chat messages, per
LuckPerms rank, so servers decide who may color text, add click or hover events,
or embed objects such as player heads.

## ADDED Requirements

### Requirement: Tags require a permission per tag type
When Butterfly formats a chat message, it SHALL interpret a MiniMessage tag in the
message text only if the sender has the permission `butterfly.chat.tag.<type>`
for that tag's type, as resolved by LuckPerms for the sender. This applies on
Paper and on Minestom.

| Type | Tags covered |
|------|--------------|
| `color` | named colours, hex colours, `<color:...>` |
| `decoration` | `<bold>`, `<italic>`, `<underlined>`, `<strikethrough>`, `<obfuscated>` and their short forms |
| `gradient` | `<gradient>` |
| `rainbow` | `<rainbow>` |
| `transition` | `<transition>` |
| `pride` | `<pride>` |
| `shadow` | `<shadow>` |
| `font` | `<font>` |
| `reset` | `<reset>` |
| `newline` | `<newline>` / `<br>` |
| `click` | `<click>` |
| `hover` | `<hover>` |
| `insertion` | `<insert>` |
| `keybind` | `<key>` |
| `translatable` | `<lang>`, `<tr>`, `<lang_or>` |
| `selector` | `<selector>` |
| `score` | `<score>` |
| `nbt` | `<nbt>` |
| `sprite` | `<sprite>` |
| `head` | `<head>` |

#### Scenario: Permitted colour
- **WHEN** a player with `butterfly.chat.tag.color` sends `<red>hello`
- **THEN** the message part of the chat line reads `hello` in red

#### Scenario: Permission for one type does not grant another
- **WHEN** a player with `butterfly.chat.tag.color` but not `butterfly.chat.tag.click` sends `<click:run_command:/op me>x</click>`
- **THEN** the chat line carries no click event

#### Scenario: Wildcard grants every type
- **WHEN** a player whose group has `butterfly.chat.tag.*` sends `<rainbow>hi</rainbow>`
- **THEN** the message is rendered with the rainbow effect

#### Scenario: Permission granted through a group
- **WHEN** the group `vip` has `butterfly.chat.tag.gradient` and a player in `vip` sends `<gradient:red:blue>hi</gradient>`
- **THEN** the message is rendered with the gradient

### Requirement: Disallowed tags stay as literal text
A tag the sender has no permission for SHALL appear in the chat line exactly as
typed, including its angle brackets and arguments. Text around it MUST NOT be
removed or altered.

#### Scenario: No permissions at all
- **WHEN** a player without any `butterfly.chat.tag.*` permission sends `<red>hello</red> a < b`
- **THEN** the message part of the chat line is the plain text `<red>hello</red> a < b`

#### Scenario: Mixed allowed and disallowed tags
- **WHEN** a player with only `butterfly.chat.tag.decoration` sends `<bold>hi</bold> <red>there`
- **THEN** `hi` is bold
- **AND** `<red>there` appears literally

### Requirement: Tag permissions do not affect the prefix
The sender's LuckPerms prefix and name in the chat line SHALL keep their
MiniMessage formatting regardless of the sender's tag permissions.

#### Scenario: Coloured prefix for a player without tag permissions
- **WHEN** a player in a group with prefix `<red>[Admin] ` and no tag permissions sends `hi`
- **THEN** the chat line shows `[Admin] ` in red followed by the name and `hi`

### Requirement: Every viewer sees the same message
The message part of a chat line SHALL be the same for every viewer; it depends
only on the sender's permissions, not on the viewer's.

#### Scenario: Two viewers
- **WHEN** a player with `butterfly.chat.tag.color` sends `<red>hi` and two other players receive it
- **THEN** both receive `hi` in red
