package garden.ephemeral.gradle.plugins.dsstore

import assertk.*
import assertk.assertions.*
import assertk.assertions.support.*
import garden.ephemeral.macfiles.dsstore.*
import garden.ephemeral.macfiles.dsstore.types.*
import org.gradle.nativeplatform.platform.internal.*
import org.gradle.testkit.runner.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.io.*
import java.awt.image.*
import java.io.*
import java.nio.file.*
import javax.imageio.*

/**
 * Integration tests for [GenerateDSStore] task.
 */
class GenerateDSStoreTest {
    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `generating store file similar to what is in a DMG`() {
        writeDummyBackgroundImage(testProjectDir.resolve("build/working-dir/.background/Background.png"))
        testProjectDir.resolve("settings.gradle.kts").writeText("")
        testProjectDir.resolve("build.gradle.kts").writeText(
            """
            import garden.ephemeral.gradle.plugins.dsstore.GenerateDSStore
            
            plugins {
                id("garden.ephemeral.dsstore")
            }

            val generateDSStore by tasks.registering(GenerateDSStore::class) {
                outputFile.set(file("${'$'}buildDir/working-dir/my-ds-store"))
                root {
                    backgroundImage.set(file("${'$'}buildDir/working-dir/.background/Background.png"))
                }
                item("Acme.app") {
                    iconLocation.set(120, 180)
                }
                item("Applications") {
                    iconLocation.set(393, 180)
                }
                item(".background") {
                    iconLocation.set(120, 350)
                }
                item(".fseventsd") {
                    iconLocation.set(393, 350)
                }
            }
            """.trimIndent()
        )

        val result: BuildResult = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments("generateDSStore", "--stacktrace")
            .build()

        val dsStoreFile = testProjectDir.resolve("build/working-dir/my-ds-store")

        if (DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
            assertThat(result.task(":generateDSStore")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
            assertThat(dsStoreFile).exists()
            // Check something we specified to be written into the file
            DSStore.open(dsStoreFile.toPath()).use { store ->
                assertThat(store["Acme.app", DSStoreProperties.IconLocation])
                    .isEqualTo(IntPoint(120, 180))
            }
        } else {
            assertThat(result.task(":generateDSStore")!!.outcome).isEqualTo(TaskOutcome.SKIPPED)
            assertThat(dsStoreFile).doesNotExist()
        }
    }

    private fun writeDummyBackgroundImage(file: File) {
        Files.createDirectories(file.parentFile.toPath())
        val image = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB)
        ImageIO.write(image, "PNG", file)
    }

    // https://github.com/willowtreeapps/assertk/issues/448
    private fun Assert<File>.doesNotExist() = given { actual ->
        if (!actual.exists()) return
        expected("not to exist")
    }
}
