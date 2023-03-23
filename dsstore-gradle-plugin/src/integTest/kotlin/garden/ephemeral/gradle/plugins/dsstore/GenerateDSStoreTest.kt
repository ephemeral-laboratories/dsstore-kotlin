package garden.ephemeral.gradle.plugins.dsstore

import assertk.assertThat
import assertk.assertions.exists
import assertk.assertions.isEqualTo
import garden.ephemeral.macfiles.dsstore.DSStore
import garden.ephemeral.macfiles.dsstore.DSStoreProperties
import garden.ephemeral.macfiles.dsstore.types.IntPoint
import garden.ephemeral.macfiles.dsstore.util.FileMode
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO

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

        assertThat(result.task(":generateDSStore")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)

        val dsStoreFile = testProjectDir.resolve("build/working-dir/my-ds-store")
        assertThat(dsStoreFile).exists()
        // Check something we specified to be written into the file
        DSStore.open(dsStoreFile.toPath()).use { store ->
            assertThat(store["Acme.app", DSStoreProperties.IconLocation])
                .isEqualTo(IntPoint(120, 180))
        }
    }

    private fun writeDummyBackgroundImage(file: File) {
        Files.createDirectories(file.parentFile.toPath())
        val image = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB)
        ImageIO.write(image, "PNG", file)
    }
}
