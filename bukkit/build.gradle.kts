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
version = "1.1.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    // LuckPerms API
    compileOnly(libs.luckperms.api)
    // Togglz
    implementation(libs.togglz)
    // API
    implementation(project(":api"))
    // Paper API
    compileOnly(libs.paper)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}


paper {
    name = "Butterfly"
    main = "net.onelitefeather.butterfly.bukkit.Butterfly"
    apiVersion = "1.19"
    version = publishData.getVersion(false)
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
        minecraftVersion("1.20.1")
    }
}

publishData {
    addBuildData()
    useGitlabReposForProject("177", "https://gitlab.themeinerlp.dev/")
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