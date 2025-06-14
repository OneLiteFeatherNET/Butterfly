import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    java
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.plugin.yml)
}
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
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
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}



tasks {
    runServer {
        minecraftVersion("1.21.4")
    }
    jar {
        archiveClassifier.set("unshaded")
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveFileName.set("${rootProject.name}-${project.name}.${archiveExtension.getOrElse("jar")}")
        archiveClassifier.set("")
        mergeServiceFiles()
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

