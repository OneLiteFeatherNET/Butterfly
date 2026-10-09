# Changelog

## [2.1.1](https://github.com/OneLiteFeatherNET/Butterfly/compare/v2.1.0...v2.1.1) (2026-10-09)


### Bug Fixes

* **deps:** update dependency net.onelitefeather:mycelium-bom to v1.8.9 ([#141](https://github.com/OneLiteFeatherNET/Butterfly/issues/141)) ([a78197b](https://github.com/OneLiteFeatherNET/Butterfly/commit/a78197b7e50b614fa7642b34df2baf5173706e8b))
* **deps:** update dependency org.slf4j:slf4j-api to v2.0.19 ([#137](https://github.com/OneLiteFeatherNET/Butterfly/issues/137)) ([16c2cdd](https://github.com/OneLiteFeatherNET/Butterfly/commit/16c2cdd84d1ab0ec29d132228fc6fc1cef86cc0a))
* **deps:** update dependency org.slf4j:slf4j-api to v2.0.20 ([#143](https://github.com/OneLiteFeatherNET/Butterfly/issues/143)) ([48d4f20](https://github.com/OneLiteFeatherNET/Butterfly/commit/48d4f20ea07738a7833fa9562eebfb27ce580e02))

## [2.1.0](https://github.com/OneLiteFeatherNET/Butterfly/compare/v2.0.0...v2.1.0) (2026-10-06)


### Features

* **chat:** show the player head before the prefix in chat ([#138](https://github.com/OneLiteFeatherNET/Butterfly/issues/138)) ([24ead74](https://github.com/OneLiteFeatherNET/Butterfly/commit/24ead749dfcb7bb4fb511adf6f6ad509b44105d2))

## [2.0.0](https://github.com/OneLiteFeatherNET/Butterfly/compare/v1.1.1...v2.0.0) (2026-10-05)


### ⚠ BREAKING CHANGES

* **config:** flags.properties is no longer read. Settings live in the data-folder config.yaml (plugins/Butterfly/config.yaml on Paper, extensions/Butterfly/config.yaml on Minestom) and are written with defaults on first start. Minestom team collision is set with butterfly.teams.collision.

### Bug Fixes

* **ci:** publish to maven when release-please creates a release ([#129](https://github.com/OneLiteFeatherNET/Butterfly/issues/129)) ([81ea743](https://github.com/OneLiteFeatherNET/Butterfly/commit/81ea743e086b8b3c8b56a4ab61cd6771a1202cd3))
* **deps:** update dependency net.onelitefeather:mycelium-bom to v1.8.7 ([#132](https://github.com/OneLiteFeatherNET/Butterfly/issues/132)) ([2737f56](https://github.com/OneLiteFeatherNET/Butterfly/commit/2737f5678344345772c3ef071267dc3e7d41231b))
* **deps:** update dependency net.onelitefeather:mycelium-bom to v1.8.8 ([#134](https://github.com/OneLiteFeatherNET/Butterfly/issues/134)) ([3401405](https://github.com/OneLiteFeatherNET/Butterfly/commit/34014053ae918322206835892801e1e33a7c62af))
* **deps:** update dependency org.slf4j:slf4j-api to v2.0.18 ([#135](https://github.com/OneLiteFeatherNET/Butterfly/issues/135)) ([7c67066](https://github.com/OneLiteFeatherNET/Butterfly/commit/7c67066e489771d51bd66396dc1b72ea808a0221))


### Code Refactoring

* **config:** replace togglz with avaje-config ([#133](https://github.com/OneLiteFeatherNET/Butterfly/issues/133)) ([50fdfd4](https://github.com/OneLiteFeatherNET/Butterfly/commit/50fdfd4e65e796ce5429c24627d6f354c17a1571))

## [1.1.1](https://github.com/OneLiteFeatherNET/Butterfly/compare/v1.1.0...v1.1.1) (2026-09-30)


### Bug Fixes

* **api:** use the player's effective luckperms prefix ([#128](https://github.com/OneLiteFeatherNET/Butterfly/issues/128)) ([31f3b91](https://github.com/OneLiteFeatherNET/Butterfly/commit/31f3b91aea03c15edfba8d7cfb271c88ddc49983))
* **minestom:** create the extension data directory before reading flags ([#125](https://github.com/OneLiteFeatherNET/Butterfly/issues/125)) ([1a02b58](https://github.com/OneLiteFeatherNET/Butterfly/commit/1a02b58369ec56594bf2a80be41fcc1048c49538))
* **minestom:** send team prefix and colour updates to online players ([#127](https://github.com/OneLiteFeatherNET/Butterfly/issues/127)) ([fc3dbe1](https://github.com/OneLiteFeatherNET/Butterfly/commit/fc3dbe12ac2eba15d80f310c3cbe1581984191ec))

## [1.1.0](https://github.com/OneLiteFeatherNET/Butterfly/compare/v1.0.26...v1.1.0) (2026-09-30)


### Features

* **minestom:** ship butterfly-minestom as a minestom extension ([#123](https://github.com/OneLiteFeatherNET/Butterfly/issues/123)) ([0521b0d](https://github.com/OneLiteFeatherNET/Butterfly/commit/0521b0df99623e390bb8cbe5146b704282599dde))


### Bug Fixes

* **deps:** update dependency net.onelitefeather:mycelium-bom to v1.8.6 ([#120](https://github.com/OneLiteFeatherNET/Butterfly/issues/120)) ([42c02ae](https://github.com/OneLiteFeatherNET/Butterfly/commit/42c02ae533e527d999b20f9a30b589e91c60c338))

## [1.0.26](https://github.com/OneLiteFeatherNET/Butterfly/compare/v1.0.25...v1.0.26) (2026-09-13)


### Bug Fixes

* **build:** include project version in shadowJar filename ([81067b0](https://github.com/OneLiteFeatherNET/Butterfly/commit/81067b046918884fff9c47bfc8b41dff89524cd4))
* **release-please:** Set project version to 1.0.25 and define group in build.gradle.kts ([8bfe7a4](https://github.com/OneLiteFeatherNET/Butterfly/commit/8bfe7a4a177a1b10ea9056ebbb5d11ad87256657))
* **release-please:** update extra-files to include build.gradle.kts ([bfe42d5](https://github.com/OneLiteFeatherNET/Butterfly/commit/bfe42d5e1f31df36b41fa5ead00387dd2715eeb3))

## [1.0.25](https://github.com/OneLiteFeatherNET/Butterfly/compare/v1.0.24...v1.0.25) (2026-09-06)


### Bug Fixes

* **deps:** update dependency net.onelitefeather:mycelium-bom to v1.8.1 ([#102](https://github.com/OneLiteFeatherNET/Butterfly/issues/102)) ([a6e2c70](https://github.com/OneLiteFeatherNET/Butterfly/commit/a6e2c707ac4ff8de83434528f603b1e35d8d0679))
* **deps:** update dependency net.onelitefeather:mycelium-bom to v1.8.2 ([#104](https://github.com/OneLiteFeatherNET/Butterfly/issues/104)) ([ac8a208](https://github.com/OneLiteFeatherNET/Butterfly/commit/ac8a2088c6c9bf9197ba14d66ca4cdcbe5eb1694))
* **deps:** update dependency net.onelitefeather:mycelium-bom to v1.8.3 ([#111](https://github.com/OneLiteFeatherNET/Butterfly/issues/111)) ([da0c3bf](https://github.com/OneLiteFeatherNET/Butterfly/commit/da0c3bf1aef69d48d225247f671e541d63280d27))
* **deps:** update dependency net.onelitefeather:mycelium-bom to v1.8.4 ([#114](https://github.com/OneLiteFeatherNET/Butterfly/issues/114)) ([468f2cc](https://github.com/OneLiteFeatherNET/Butterfly/commit/468f2cceaea2c4b5f5720eca6cd438ed42b59c27))
* **deps:** update dependency net.onelitefeather:mycelium-bom to v1.8.5 ([#115](https://github.com/OneLiteFeatherNET/Butterfly/issues/115)) ([7ffdc33](https://github.com/OneLiteFeatherNET/Butterfly/commit/7ffdc33f79871e7b2465d6c4965a0cc352c6c798))
* **deps:** update dependency org.togglz:togglz-core to v4.6.3 ([#108](https://github.com/OneLiteFeatherNET/Butterfly/issues/108)) ([6d66d77](https://github.com/OneLiteFeatherNET/Butterfly/commit/6d66d773a7cfca12593f659834a972b031d50544))
* **deps:** update dependency org.togglz:togglz-core to v4.6.4 ([#110](https://github.com/OneLiteFeatherNET/Butterfly/issues/110)) ([36b5e53](https://github.com/OneLiteFeatherNET/Butterfly/commit/36b5e53ca8e6cb4b6113d4842f5f5367fa9e1fcb))

## [1.0.24](https://github.com/OneLiteFeatherNET/Butterfly/compare/v1.0.23...v1.0.24) (2026-06-29)


### Bug Fixes

* **deps:** update dependency net.onelitefeather:mycelium-bom to v1.6.5 ([#85](https://github.com/OneLiteFeatherNET/Butterfly/issues/85)) ([6751d38](https://github.com/OneLiteFeatherNET/Butterfly/commit/6751d38615e897e3e37e2c9eda10a1ceaae4fa57))
* **deps:** update dependency net.onelitefeather:mycelium-bom to v1.6.6 ([#88](https://github.com/OneLiteFeatherNET/Butterfly/issues/88)) ([8351743](https://github.com/OneLiteFeatherNET/Butterfly/commit/835174304cfcca6760a35ef59a895a1a6d07c829))
* **deps:** update dependency net.onelitefeather:mycelium-bom to v1.6.7 ([#89](https://github.com/OneLiteFeatherNET/Butterfly/issues/89)) ([af7edc6](https://github.com/OneLiteFeatherNET/Butterfly/commit/af7edc68ae376d622bf387c266e15f596ba7633b))
* **deps:** update dependency org.togglz:togglz-core to v4.6.2 ([#91](https://github.com/OneLiteFeatherNET/Butterfly/issues/91)) ([9bfa2b7](https://github.com/OneLiteFeatherNET/Butterfly/commit/9bfa2b70fe1cb96b08e96ac56c01511e43ea3e43))
* **deps:** update mycelium-bom ([6d4bc70](https://github.com/OneLiteFeatherNET/Butterfly/commit/6d4bc706a3505e99baadffb94e8b2974125e03a7))
* **deps:** update mycelium-bom (patch) ([#98](https://github.com/OneLiteFeatherNET/Butterfly/issues/98)) ([6d4bc70](https://github.com/OneLiteFeatherNET/Butterfly/commit/6d4bc706a3505e99baadffb94e8b2974125e03a7))
* **deps:** update mycelium-bom to v1.7.0 ([#93](https://github.com/OneLiteFeatherNET/Butterfly/issues/93)) ([2f6792e](https://github.com/OneLiteFeatherNET/Butterfly/commit/2f6792ed36eab495f8c3146224ff2e3810ba2a83))
* **deps:** update mycelium-bom to v1.7.1 ([#96](https://github.com/OneLiteFeatherNET/Butterfly/issues/96)) ([fd4969b](https://github.com/OneLiteFeatherNET/Butterfly/commit/fd4969bdc30f1ccedae87a77602d3bff2398d9a0))
