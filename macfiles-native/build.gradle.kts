import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    kotlin("jvm")
    id("common.publishing")
}

description = "Additional native support for creating some macOS file formats"

repositories {
    mavenCentral()
}

dependencies {
    // This module directly returns types defined in macfiles
    api(project(":macfiles"))

    implementation("net.java.dev.jna:jna:5.13.0")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testImplementation("com.willowtreeapps.assertk:assertk:0.25")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
