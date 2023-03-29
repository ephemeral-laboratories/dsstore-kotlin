import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    id("com.netflix.nebula.integtest") version "10.1.2"
    id("com.gradle.plugin-publish") version "1.1.0"
    id("common.publishing")
}

description = "Gradle plugin to create macOS .DS_Store files, primarily for putting inside DMGs"

repositories {
    mavenCentral()
}

dependencies {
    // We deliberately don't want to expose the underlying library as API,
    // so we can swap it out if someone, say, writes a more complete library.
    implementation(project(":macfiles"))
    implementation(project(":macfiles-native"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testImplementation("com.willowtreeapps.assertk:assertk:0.25")

    integTestImplementation(kotlin("test"))
    integTestImplementation(gradleTestKit())
    integTestImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    integTestImplementation("com.willowtreeapps.assertk:assertk:0.25")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

gradlePlugin {
    plugins {
        create("dsstore") {
            id = "garden.ephemeral.dsstore"
            displayName = ".DS_Store generator plugin"
            description = project.description
            implementationClass = "garden.ephemeral.gradle.plugins.dsstore.DSStorePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/ephemeral-laboratories/dsstore-kotlin"
    vcsUrl = "https://github.com/ephemeral-laboratories/dsstore-kotlin"
    tags = listOf("macos", "dsstore")
}
