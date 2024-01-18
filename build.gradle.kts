
import de.chojo.PublishData

plugins {
    java
    alias(libs.plugins.publishdata)
    `maven-publish`
}

group = "net.onelitefeather"
version = "1.1.1-SNAPSHOT"
allprojects {
    apply {
        plugin<JavaPlugin>()
        plugin<PublishData>()
        plugin<MavenPublishPlugin>()
    }
}