plugins {
    java
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    // Togglz
    implementation(libs.togglz)
    // LuckPerms API
    compileOnly(libs.luckperms.api)
    // API
    implementation(project(":api"))
    // Microtus
    implementation(platform(libs.microtus.bom))
    compileOnly(libs.microtus)
}

tasks {

    compileJava {
        options.release.set(21)
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
    useGitlabReposForProject("177", "https://gitlab.onelitefeather.dev/")
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