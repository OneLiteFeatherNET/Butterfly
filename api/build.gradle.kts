plugins {
    java
}

dependencies {
    compileOnly(libs.luckperms.api)

    testImplementation(platform(libs.mycelium.bom))
    testImplementation(libs.luckperms.api)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit)
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
