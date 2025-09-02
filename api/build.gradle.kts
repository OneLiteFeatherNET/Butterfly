plugins {
    id("java")
}
dependencies {
    compileOnly(libs.luckperms.api)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    jar {
        archiveClassifier.set("unshaded")
    }
}
