rootProject.name = "Butterfly"
include("api")
include("bukkit")
include("minestom")
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://eldonexus.de/repository/maven-public/")
    }
}


dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("paper", "1.20.4-R0.1-SNAPSHOT")
            version("plugin.yml", "0.6.0")
            version("run-paper", "2.3.0")
            version("publishdata", "1.3.0")
            version("shadow", "8.1.1")
            version("luckperms", "5.4")
            version("adventure", "4.14.0")
            version("minestom", "1.1.1")
            version("togglz", "4.4.0")

            // Paper
            library("paper", "io.papermc.paper", "paper-api").versionRef("paper")
            library("luckperms.api", "net.luckperms", "api").versionRef("luckperms")
            library("adventure.minimessage", "net.kyori", "adventure-text-minimessage").versionRef("adventure")
            library("minestom", "net.onelitefeather.microtus", "Minestom").versionRef("minestom")
            library("togglz", "org.togglz", "togglz-core").versionRef("togglz")

            plugin("plugin.yml", "net.minecrell.plugin-yml.paper").versionRef("plugin.yml")
            plugin("run.paper", "xyz.jpenilla.run-paper").versionRef("run-paper")
            plugin("publishdata", "de.chojo.publishdata").versionRef("publishdata")
            plugin("shadow", "com.github.johnrengelman.shadow").versionRef("shadow")
        }
    }
}
