import de.chojo.Repo
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    java
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.plugin.yml)
    alias(libs.plugins.publishdata)
    `maven-publish`
}
group = "net.onelitefeather"
version = "1.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    // LuckPerms API
    compileOnly(libs.luckperms.api)
    // API
    implementation(project(":api"))
    // Paper API
    compileOnly(libs.paper)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


paper {
    name = "Butterfly"
    main = "net.onelitefeather.butterfly.bukkit.Butterfly"
    apiVersion = "1.19"
    version = publishData.getVersion(true)
    author = "TheMeinerLP"
    authors = listOf("theShadowsDust")
    serverDependencies {
        register("LuckPerms") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
    }
}


tasks {
    runServer {
        minecraftVersion("1.20.4")
    }

    compileJava {
        options.release.set(17)
        options.encoding = "UTF-8"
    }
}

publishData {
    addBuildData()
    addRepo(Repo(Regex(".*"), " SNAPSHOT", "https://gitlab.themeinerlp.dev/api/v4/projects/177/packages/maven", false, Repo.Type.SNAPSHOT))
    addRepo(Repo(Regex("master"), "", "https://gitlab.themeinerlp.dev/api/v4/projects/177/packages/maven", false, Repo.Type.STABLE))
    publishTask("shadowJar")
}

publishing {
    publications.create<MavenPublication>("maven") {
        // configure the publication as defined previously.
        publishData.configurePublication(this)
        version = publishData.getVersion(false)
    }

    repositories {
        maven {
            credentials(HttpHeaderCredentials::class) {
                name = "Job-Token"
                value = System.getenv("CI_JOB_TOKEN")
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }


            name = "Gitlab"
            // Get the detected repository from the publish data
            url = uri(publishData.getRepository())
        }
    }
}