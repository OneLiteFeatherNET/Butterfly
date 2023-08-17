plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    // Service Loader
    implementation("com.google.auto.service:auto-service:1.1.1")
    // LuckPerms API
    compileOnly("net.luckperms:api:5.4")
    // API
    implementation(project(":api"))
    // Minestom
    compileOnly("net.onelitefeather.microtus:Minestom:1.1.1-SNAPSHOT+a8e43bb")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}