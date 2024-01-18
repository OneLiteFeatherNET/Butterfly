import de.chojo.Repo

plugins {
    java
    `maven-publish`
    alias(libs.plugins.publishdata)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

group = "net.onelitefeather"
version = "1.1.0"

dependencies {
    // Minimessage
    implementation(libs.adventure.minimessage)
    // Togglz
    implementation(libs.togglz)
    // LuckPerms API
    compileOnly(libs.luckperms.api)
    // API
    implementation(project(":api"))
    // Minestom
    compileOnly(libs.minestom)
}

tasks {

    compileJava {
        options.release.set(17)
        options.encoding = "UTF-8"
    }
}

publishData {
    addBuildData()
    addRepo(Repo(Regex(".*"), "SNAPSHOT", "https://gitlab.themeinerlp.dev/api/v4/projects/177/packages/maven", false, Repo.Type.SNAPSHOT))
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