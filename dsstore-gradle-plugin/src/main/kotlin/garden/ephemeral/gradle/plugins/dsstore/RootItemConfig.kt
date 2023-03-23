package garden.ephemeral.gradle.plugins.dsstore

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

/**
 * Holder for configuration for a single item in the directory.
 */
abstract class RootItemConfig {
    /**
     * Specifies the background image to use.
     *
     * If `null`, will default to a plain coloured background.
     */
    @get:InputFile
    @get:Optional
    abstract val backgroundImage: RegularFileProperty

    /**
     * Specifies the size of the window.
     */
    @get:Nested
    @get:Optional
    abstract val windowSize: Property<WindowSize>

    /**
     * Extension convenience method for DSL to set width and height without explicitly
     * constructing the location object.
     *
     * @param width the width.
     * @param height the height.
     */
    fun Property<WindowSize>.set(width: Int, height: Int) = set(WindowSize(width, height))

    data class WindowSize(
        @get:Input
        val width: Int,
        @get:Input
        val height: Int
    )
}
