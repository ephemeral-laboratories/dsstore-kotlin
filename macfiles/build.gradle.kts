import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
}

version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // XXX: Okio is the path to multiplatform, but it is an awkward API when
    //      you want to read from / write to the same file.
    // implementation("com.squareup.okio:okio:3.2.0")
    implementation("com.googlecode.plist:dd-plist:1.26")

    testImplementation(kotlin("test"))
    testImplementation("com.willowtreeapps.assertk:assertk:0.25")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
