import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    java
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.plugin.yml)
}
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



tasks {
    runServer {
        minecraftVersion("1.20.1")
    }
    shadowJar {
        mergeServiceFiles()
        archiveVersion.set(rootProject.version as String)
    }
    build {
        dependsOn(shadowJar)
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

