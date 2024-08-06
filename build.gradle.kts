
import de.chojo.PublishData

plugins {
    java
    alias(libs.plugins.publishdata)
    `maven-publish`
}

var publishModules = setOf("bukkit", "minestom")


group = "net.onelitefeather"
version = "1.2.0"
allprojects {
    apply {
        plugin<JavaPlugin>()
        plugin<PublishData>()
        if (publishModules.contains(project.name)) {
            plugin<MavenPublishPlugin>()
        }
    }
}