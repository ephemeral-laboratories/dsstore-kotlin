import org.gradle.api.publish.maven.*
import org.gradle.kotlin.dsl.*

// Publishing configuration we want to share between multiple projects

plugins {
    java
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

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        // This is defined here to capture all publications, including those added by
        // Gradle's plugin publishing plugin.
        withType<MavenPublication> {
            pom {
                val projectGitUrl = "https://github.com/ephemeral-laboratories/dsstore-kotlin"
                name.set(project.name)
                description.set(project.description)
                url.set(projectGitUrl)
                licenses {
                    license {
                        name.set("MIT")
                        url.set("$projectGitUrl/blob/main/COPYING.txt")
                    }
                }
                developers {
                    developer {
                        id.set("hakanai")
                        name.set("Hakanai")
                        email.set("hakanai@ephemeral.garden")
                        url.set("https://linktr.ee/hakanai")
                    }
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("$projectGitUrl/issues")
                }
                scm {
                    url.set(projectGitUrl)
                    connection.set("scm:git:$projectGitUrl")
                    developerConnection.set("scm:git:$projectGitUrl")
                }
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
