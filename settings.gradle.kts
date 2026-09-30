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
        // minestom-extensions lives here and needs no credentials.
        maven {
            name = "OneLiteFeatherReleases"
            url = uri("https://repo.onelitefeather.dev/releases")
        }
    }
    versionCatalogs {
        create("libs") {
            version("paper", "26.1.2.build.+")
            version("plugin.yml", "0.6.0")
            version("run-paper", "3.1.0")
            version("shadow", "9.6.1")
            version("togglz", "4.6.4")
            version("mycelium-bom", "1.8.6")
            version("luckperms.api", "5.6-SNAPSHOT")
            version("minestom-extensions", "2.2.0")
            // must match the Minestom version resolved through the BOMs (Env lives in this artifact)
            version("minestom-testing", "2026.09.12-26.2")

            // Paper
            library("paper", "io.papermc.paper", "paper-api").versionRef("paper")
            library("mycelium-bom", "net.onelitefeather", "mycelium-bom").versionRef("mycelium-bom")
            library("cyano", "net.onelitefeather", "cyano").withoutVersion()
            library("minestom","net.minestom", "minestom").withoutVersion()
            library("minestom.testing", "net.minestom", "testing").versionRef("minestom-testing")
            library("adventure.minimessage", "net.kyori", "adventure-text-minimessage").withoutVersion()
            library("togglz", "org.togglz", "togglz-core").versionRef("togglz")
            library("luckperms.api", "net.luckperms", "api").versionRef("luckperms.api")
            library("minestom-extensions-bom", "net.onelitefeather", "minestom-extensions-bom").versionRef("minestom-extensions")
            library("minestom-extensions", "net.onelitefeather", "minestom-extensions").withoutVersion()
            library("minestom-extensions-processor", "net.onelitefeather", "minestom-extensions-processor").withoutVersion()

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
