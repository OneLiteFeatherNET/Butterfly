plugins {
    java
}

dependencies {
    compileOnly(libs.luckperms.api)
    // provided by Paper and Minestom at runtime
    compileOnly(libs.slf4j.api)
    implementation(libs.avaje.config)

    testImplementation(platform(libs.mycelium.bom))
    testImplementation(libs.slf4j.api)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    jar {
        archiveClassifier.set("unshaded")
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
