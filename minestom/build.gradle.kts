plugins {
    java
    alias(libs.plugins.shadow)
    `maven-publish`
    jacoco
}

dependencies {
    // API
    implementation(project(":api"))
    // Minestom
    implementation(platform(libs.mycelium.bom))
    // Togglz
    implementation(libs.togglz)
    // LuckPerms API
    compileOnly(libs.luckperms.api)
    compileOnly(libs.minestom)
    compileOnly(libs.adventure.minimessage)

    testImplementation(platform(libs.mycelium.bom))
    testImplementation(libs.minestom)
    testImplementation(libs.minestom.testing)
    testImplementation(libs.adventure.minimessage)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

// net.minestom:testing is not managed by mycelium-bom, so its version is pinned in the
// version catalog. It has to stay on the same build as the Minestom the BOM resolves, or
// the test harness runs against a different server core than the code under test.
val checkMinestomTestingVersion = tasks.register("checkMinestomTestingVersion") {
    val testingVersion = libs.versions.minestom.testing.get()
    val resolvedMinestom = configurations.testCompileClasspath.map { classpath ->
        classpath.resolvedConfiguration.resolvedArtifacts
            .map { it.moduleVersion.id }
            .first { it.group == "net.minestom" && it.name == "minestom" }
            .version
    }
    doLast {
        val minestomVersion = resolvedMinestom.get()
        require(minestomVersion == testingVersion) {
            "net.minestom:testing is pinned to $testingVersion but net.minestom:minestom resolves " +
                    "to $minestomVersion. Update the minestom.testing version in settings.gradle.kts."
        }
    }
}

tasks {
    jar {
        archiveClassifier.set("unshaded")
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("butterfly-minestom.jar")
        mergeServiceFiles()
    }
    test {
        dependsOn(checkMinestomTestingVersion)
        useJUnitPlatform()
        finalizedBy(project.tasks.jacocoTestReport)
        jvmArgs("-Dminestom.inside-test=true")
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    jacocoTestReport {
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
    }
}
publishing {
    publications.create<MavenPublication>("maven") {
        artifact(project.tasks.getByName("shadowJar"))
        version = rootProject.version as String
        artifactId = "butterfly-minestom"
        groupId = rootProject.group as String
        pom {
            name = "Butterfly Minestom Library"
            description = "A simple library to support luckperms prefix, suffix and more in Minestom."
            url = "https://github.com/OneLiteFeatherNET/Butterfly"
            licenses {
                license {
                    name = "The Apache License, Version 2.0"
                    url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }
            developers {
                developer {
                    id = "themeinerlp"
                    name = "Phillipp Glanz"
                    email = "p.glanz@madfix.me"
                }
            }
            scm {
                connection = "scm:git:git://github.com:OneLiteFeatherNET/Butterfly.git"
                developerConnection = "scm:git:ssh://git@github.com:OneLiteFeatherNET/Butterfly.git"
                url = "https://github.com/OneLiteFeatherNET/Butterfly"
            }
        }
    }

    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    // Those credentials need to be set under "Settings -> Secrets -> Actions" in your repository
                    username = System.getenv("ONELITEFEATHER_MAVEN_USERNAME")
                    password = System.getenv("ONELITEFEATHER_MAVEN_PASSWORD")
                }
            }

            name = "OneLiteFeatherRepository"
            val releasesRepoUrl = uri("https://repo.onelitefeather.dev/onelitefeather-releases")
            val snapshotsRepoUrl = uri("https://repo.onelitefeather.dev/onelitefeather-snapshots")
            url = if (version.toString().contains("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}