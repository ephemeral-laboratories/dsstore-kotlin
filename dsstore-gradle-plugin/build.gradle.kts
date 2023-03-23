import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    `java-gradle-plugin`
    `jvm-test-suite`
    id("com.netflix.nebula.integtest") version "10.1.2"
}

version = "0.1-SNAPSHOT"

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
            implementationClass = "garden.ephemeral.gradle.plugins.dsstore.DSStorePlugin"
        }
    }
}
