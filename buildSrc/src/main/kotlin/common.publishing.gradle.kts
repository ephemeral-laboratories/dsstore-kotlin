import org.gradle.api.publish.maven.*
import org.gradle.kotlin.dsl.*

// Publishing configuration we want to share between multiple projects

plugins {
    `maven-publish`
    signing
}

val isReleaseVersion by project.extra {
    // Relies on a command-line-provided system property, to mitigate the risk of
    // accidentally treating arbitrary main branch commits as a release
    // XXX: Could pass the actual tag and check that it and the version match?
    val isRealRelease = project.findProperty("realRelease") == "true"
    val isSnapshotVersion = project.version.toString().endsWith("-SNAPSHOT")
    isRealRelease && !isSnapshotVersion
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
            }
        }
    }

    signing.sign(publications)

    repositories {
        maven {
            url = if (isReleaseVersion) {
                uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
            } else {
                uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
            }

            val sonatypeUsername = findProperty("sonatypeUsername") as? String ?: System.getenv("SONATYPE_USERNAME")
            val sonatypePassword = findProperty("sonatypePassword") as? String ?: System.getenv("SONATYPE_PASSWORD")
            if (sonatypeUsername != null && sonatypePassword != null) {
                credentials {
                    username = sonatypeUsername
                    password = sonatypePassword
                }
            }
        }
    }
}
