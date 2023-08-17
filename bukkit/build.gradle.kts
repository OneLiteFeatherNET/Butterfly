import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.1.0"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("org.sonarqube") version "4.3.0.3225"
    jacoco
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    // Service Loader
    implementation("com.google.auto.service:auto-service:1.1.1")
    // LuckPerms API
    compileOnly("net.luckperms:api:5.4")
    // API
    implementation(project(":api"))
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    // Testing
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


paper {
    main = "net.onelitefeather.butterfly.bukkit.Butter"
    apiVersion = "1.20"
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

    compileJava {
        options.release.set(17)
        options.encoding = "UTF-8"
    }
    jacocoTestReport {
        dependsOn(project.tasks.test)
        reports {
            xml.required.set(true)
        }
    }

    test {
        finalizedBy(project.tasks.jacocoTestReport)
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    getByName<org.sonarqube.gradle.SonarTask>("sonar") {
        dependsOn(project.tasks.test)
    }
    shadowJar {
        archiveFileName.set("${rootProject.name}.${archiveExtension.getOrElse("jar")}")
    }
}