plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.run-paper") version "2.0.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("org.sonarqube") version "4.0.0.2929"
    jacoco

}

group = "net.onelitefeather"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = uri("https://papermc.io/repo/repository/maven-public/"))
    maven(url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/"))
}

dependencies {
    // Paper
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
    implementation("cloud.commandframework:cloud-annotations:1.7.1")
    annotationProcessor("cloud.commandframework:cloud-annotations:1.7.1")
    implementation("cloud.commandframework", "cloud-minecraft-extras", "1.7.0")
    library("com.google.code.gson", "gson", "2.8.7") // All platforms
    bukkitLibrary("com.google.code.gson", "gson", "2.8.7") // Bukkit only
    implementation("net.kyori:adventure-api:4.12.0")
    implementation ("cloud.commandframework:cloud-paper:1.8.0")

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

bukkit {
    main = "net.onelitefeather.butterfly.Butterfly"
    apiVersion = "1.19"
}


tasks {
    runServer {
        minecraftVersion("1.19.3")
    }

    compileJava {
        options.release.set(17)
        options.encoding = "UTF-8"
    }
    jacocoTestReport {
        dependsOn(rootProject.tasks.test)
        reports {
            xml.required.set(true)
        }
    }

    test {
        finalizedBy(rootProject.tasks.jacocoTestReport)
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    getByName<org.sonarqube.gradle.SonarTask>("sonar") {
        dependsOn(rootProject.tasks.test)
    }
    shadowJar {
        archiveFileName.set("${rootProject.name}.${archiveExtension.getOrElse("jar")}")
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "onelitefeather_projects_butterfly_AYZ5KHlm528bDWin57d_")
    }
}


