rootProject.name = "Butterfly"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        maven {
            name = "OneLiteFeatherRepository"
            url = uri("https://repo.onelitefeather.dev/onelitefeather")
            if (System.getenv("CI") != null) {
                credentials {
                    username = System.getenv("ONELITEFEATHER_MAVEN_USERNAME")
                    password = System.getenv("ONELITEFEATHER_MAVEN_PASSWORD")
                }
            } else {
                credentials(PasswordCredentials::class)
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
        maven("https://repo.papermc.io/repository/maven-public/")
    }
    versionCatalogs {
        create("libs") {
            version("paper", "26.1.2.build.+")
            version("plugin.yml", "0.6.0")
            version("run-paper", "3.1.0")
            version("shadow", "9.6.1")
            version("togglz", "4.6.4")
            version("mycelium-bom", "1.8.5")
            version("luckperms.api", "5.6-SNAPSHOT")
            // Only used for the Minestom test harness. mycelium-bom pins net.minestom:minestom
            // but not net.minestom:testing, and the two have to be the same build; the
            // :minestom:checkMinestomTestingVersion task fails the build if they drift.
            version("minestom.testing", "2026.08.28-26.2")

            // Paper
            library("paper", "io.papermc.paper", "paper-api").versionRef("paper")
            library("mycelium-bom", "net.onelitefeather", "mycelium-bom").versionRef("mycelium-bom")
            library("cyano", "net.onelitefeather", "cyano").withoutVersion()
            library("minestom","net.minestom", "minestom").withoutVersion()
            library("minestom.testing","net.minestom", "testing").versionRef("minestom.testing")
            library("adventure.minimessage", "net.kyori", "adventure-text-minimessage").withoutVersion()
            library("togglz", "org.togglz", "togglz-core").versionRef("togglz")
            library("luckperms.api", "net.luckperms", "api").versionRef("luckperms.api")

            library("mockito.core", "org.mockito", "mockito-core").withoutVersion()
            library("mockito.junit", "org.mockito", "mockito-junit-jupiter").withoutVersion()

            library("junit.api", "org.junit.jupiter", "junit-jupiter-api").withoutVersion()
            library("junit.engine", "org.junit.jupiter", "junit-jupiter-engine").withoutVersion()
            library("junit.platform.launcher", "org.junit.platform", "junit-platform-launcher").withoutVersion()
            library("junit.params", "org.junit.jupiter", "junit-jupiter-params").withoutVersion()

            plugin("plugin.yml", "net.minecrell.plugin-yml.paper").versionRef("plugin.yml")
            plugin("run.paper", "xyz.jpenilla.run-paper").versionRef("run-paper")
            plugin("shadow", "com.gradleup.shadow").versionRef("shadow")
        }
    }
}
include("api")
include("bukkit")
include("minestom")
