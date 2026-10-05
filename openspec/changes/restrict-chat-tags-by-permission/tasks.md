# Tasks

Start after PR #133 (`replace-togglz-with-avaje-config`) is merged. Branch from
the up-to-date `origin/main`. Implementation commits use `feat(chat)`; the commit
that switches both platforms to the restricted parser is `feat(chat)!` with a
`BREAKING CHANGE:` footer.

## 1. Groundwork in `api`

- [x] 1.1 Add Adventure API and MiniMessage as `compileOnly` and `testImplementation` to `api/build.gradle.kts` (versions via the mycelium BOM); verify `./gradlew :api:compileJava` succeeds
- [ ] 1.2 Confirm which `StandardTags` factory provides the `<head>` tag (expected `sequentialHead()`) and the exact tag names of every row in the spec table with a parameterized unit test that parses one sample per type with only that resolver enabled; verify `./gradlew :api:test` passes
- [ ] 1.3 Add `LuckPermsAPI.hasPermission(UUID, String)` (user cached permission data, user's contextual query options, `false` when the user is not loaded) with unit tests against a fake LuckPerms user; verify `./gradlew :api:test` passes

## 2. Parser

- [ ] 2.1 Write failing unit tests for `ChatMessageParser.parse(raw, hasPermission)` covering every scenario of "Tags require a permission per tag type" and "Disallowed tags stay as literal text" (permissions as a plain `Predicate<String>`, results compared via the plain-text serializer and component style/click/hover assertions); verify they fail with `./gradlew :api:test`
- [ ] 2.2 Implement `ChatTagType` (type name -> resolvers) and `ChatMessageParser`; verify `./gradlew :api:test` passes

## 3. Platforms

- [ ] 3.1 Minestom: write failing `ExtensionBehaviourTest` cases (fresh `Env`, `FakeLuckPerms` with/without `butterfly.chat.tag.color`): coloured message with permission, literal `<red>` without, prefix keeps its colour without tag permissions; then use `ChatMessageParser` in `Butterfly#playerChat`; verify `./gradlew :minestom:test` passes
- [ ] 3.2 Paper: parse once in `PlayerListener#handleChat` with `ChatMessageParser` and a LuckPerms-backed predicate for the source player, renderer only composes; verify `./gradlew :bukkit:build` succeeds and `grep -n "MiniMessage.miniMessage()" bukkit/src/main/java/net/onelitefeather/butterfly/bukkit/listener/PlayerListener.java` returns nothing
- [ ] 3.3 Run `./gradlew build :minestom:smokeTest`; verify both succeed

## 4. Docs and manual check

- [ ] 4.1 README: section listing every `butterfly.chat.tag.<type>` node (generated from or checked against `ChatTagType`), the wildcard, the literal-text behaviour and the migration line; verify every type in the spec table appears
- [ ] 4.2 Manual Paper test via `./gradlew :bukkit:runServer` with LuckPerms: player without nodes sees `<red>x` literally; after `lp group default permission set butterfly.chat.tag.color true` the same message is red; `<click:run_command:/help>x</click>` has no click event without `butterfly.chat.tag.click` (report in the PR description)

## 5. Pull request

- [ ] 5.1 Open the pull request titled `feat(chat)!: restrict minimessage tags in chat by permission` with a `BREAKING CHANGE:` section (formatting now needs `butterfly.chat.tag.<type>`; grant `butterfly.chat.tag.*` to `default` to restore the old behaviour); verify `gh pr view` shows the title and CI is green
