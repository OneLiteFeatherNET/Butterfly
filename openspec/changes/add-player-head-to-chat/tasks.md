# Tasks

Start after `restrict-chat-tags-by-permission` is merged (and therefore #133).
Branch from the up-to-date `origin/main`. All implementation commits use
`feat(chat)`.

## 1. Setting

- [ ] 1.1 Write failing `ButterflySettingsTest` cases for `butterfly.chat.head.enabled` (absent -> `true`, `false` -> `false`, `maybe` -> `true` + warning naming key and value); implement the field in `ButterflySettings`; verify `./gradlew :api:test` passes
- [ ] 1.2 Add `butterfly.chat.head.enabled: true` with a comment to both `default-config.yaml` resources; verify the existing tests that compare the written default file still pass with `./gradlew build`

## 2. Head and chat line in `api`

- [ ] 2.1 Write failing unit tests for `PlayerHeads.of(...)` (UUID and name set; `textures` property with value and signature when given; no property when texture is `null`) and `ChatLine.compose(...)` (head + space + name + `: ` + message; no head and no leading space when head is `null`); verify they fail with `./gradlew :api:test`
- [ ] 2.2 Implement `PlayerHeads` and `ChatLine`; verify `./gradlew :api:test` passes

## 3. Platforms

- [ ] 3.1 Minestom: write failing `ExtensionBehaviourTest` cases (fresh `Env`): the formatted chat line's first child is a player-head component with the sender's UUID; with settings `chatHeadEnabled=false` there is none and the plain text equals today's; the player's display name has no head component. Then build the head from `Player#getSkin()` and compose with `ChatLine` in `Butterfly#playerChat`; verify `./gradlew :minestom:test` passes
- [ ] 3.2 Paper: in `PlayerListener#handleChat` build the head once from `getPlayerProfile()` (`textures` property) when `settings.chatHeadEnabled()`, and compose with `ChatLine` in the renderer; pass `ButterflySettings` into `PlayerListener` by constructor; verify `./gradlew :bukkit:build` succeeds
- [ ] 3.3 Run `./gradlew build :minestom:smokeTest`; verify both succeed

## 4. Docs and manual checks

- [ ] 4.1 README: document `butterfly.chat.head.enabled` in the configuration section and the client requirement (1.21.9+ for native rendering); verify the key appears in the README
- [ ] 4.2 Manual Paper test via `./gradlew :bukkit:runServer` with LuckPerms: a current client shows the sender's own skin head before the prefix; with `butterfly.chat.head.enabled: false` it disappears after restart; tab list and nametag show no head; with ViaVersion + ViaBackwards and a client older than 1.21.9, record what the line looks like (report both in the PR description)

## 5. Pull request

- [ ] 5.1 Open the pull request titled `feat(chat): show the player head before the prefix in chat` including the manual-test results; verify `gh pr view` shows the title and CI is green
