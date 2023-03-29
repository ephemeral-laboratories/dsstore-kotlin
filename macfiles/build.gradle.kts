import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    kotlin("jvm") version "1.7.20"
    id("common.publishing")
}

group = "garden.ephemeral.dsstore"
version = "0.0.3"
description = "Support for reading/writing various macOS file formats"

repositories {
    mavenCentral()
}

dependencies {
    // XXX: Okio is the path to multiplatform, but it is an awkward API when
    //      you want to read from / write to the same file.
    // implementation("com.squareup.okio:okio:3.2.0")
    implementation("com.googlecode.plist:dd-plist:1.26")

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
