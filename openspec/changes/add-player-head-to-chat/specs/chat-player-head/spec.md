# Spec Delta

## Purpose

Shows the sender's player head as an inline glyph in front of each chat line that
Butterfly formats, so players can recognise who is talking at a glance.

## ADDED Requirements

### Requirement: Head precedes the prefix in chat
When Butterfly formats a chat line and `butterfly.chat.head.enabled` is `true`,
the line SHALL start with a player-head object component of the sender, followed
by a single space, then the sender's prefix and name, `: ` and the message. This
applies on Paper and on Minestom.

#### Scenario: Player with prefix chats
- **WHEN** a player `Steve` with prefix `[Admin] ` sends `hello`
- **THEN** the first part of the chat line is a player-head component for `Steve`'s UUID
- **AND** the plain text of the rest of the line is ` [Admin] Steve: hello`

#### Scenario: Every viewer sees the head
- **WHEN** a player sends a message that two other players receive
- **THEN** both receive the line with the sender's head in front

### Requirement: Head carries the sender's skin
The head component SHALL identify the sender by UUID and name and SHALL carry the
sender's skin texture property (value and signature) when the server knows it.
When no texture is known, the component SHALL still be sent with UUID and name.

#### Scenario: Online-mode player
- **WHEN** a player whose profile has a `textures` property sends a message
- **THEN** the head component contains that `textures` value and signature

#### Scenario: Player without textures
- **WHEN** a player whose profile has no `textures` property sends a message
- **THEN** the head component contains the player's UUID and name and no texture property

### Requirement: Head only in chat
Adding the head MUST NOT change the player's display name, tab list name or team
prefix.

#### Scenario: Tab list unchanged
- **WHEN** a player joins with the head feature enabled
- **THEN** the player's tab list name contains no player-head component

### Requirement: Head can be disabled
With `butterfly.chat.head.enabled` set to `false`, chat lines SHALL be formatted
without the head and without the leading space.

#### Scenario: Disabled
- **WHEN** `butterfly.chat.head.enabled` is `false` and a player with prefix `[Admin] ` sends `hello`
- **THEN** the plain text of the chat line is `[Admin] Steve: hello` and it contains no player-head component
