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
            version("run-paper", "2.3.1")
            version("publishdata", "1.4.0")
            version("shadow", "8.1.1")
            version("luckperms", "5.5")
            version("microtus-bom", "1.5.1")
            version("togglz", "4.4.0")

            // Paper
            library("paper", "io.papermc.paper", "paper-api").versionRef("paper")
            library("luckperms.api", "net.luckperms", "api").versionRef("luckperms")
            library("microtus-bom", "net.onelitefeather.microtus", "bom").versionRef("microtus-bom")
            library("microtus", "net.onelitefeather.microtus", "Microtus").withoutVersion()
            library("togglz", "org.togglz", "togglz-core").versionRef("togglz")

            plugin("plugin.yml", "net.minecrell.plugin-yml.paper").versionRef("plugin.yml")
            plugin("run.paper", "xyz.jpenilla.run-paper").versionRef("run-paper")
            plugin("publishdata", "de.chojo.publishdata").versionRef("publishdata")
            plugin("shadow", "com.github.johnrengelman.shadow").versionRef("shadow")
        }
    }
}
