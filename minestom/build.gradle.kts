import de.chojo.Repo

plugins {
    java
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

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
    shadowJar {
        archiveVersion.set(rootProject.version as String)
    }
    build {
        dependsOn(shadowJar)
    }
}

publishData {
    addBuildData()
    addRepo(Repo(Regex("main"), "", "https://gitlab.themeinerlp.dev/api/v4/projects/177/packages/maven", false, Repo.Type.STABLE))
    addRepo(Repo(Regex("master"), "", "https://gitlab.themeinerlp.dev/api/v4/projects/177/packages/maven", false, Repo.Type.STABLE))
    addRepo(Repo(Regex(".*"), "SNAPSHOT", "https://gitlab.themeinerlp.dev/api/v4/projects/177/packages/maven", true, Repo.Type.SNAPSHOT))
    publishTask("shadowJar")
}

publishing {
    publications.create<MavenPublication>("maven") {
        // configure the publication as defined previously.
        publishData.configurePublication(this)
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