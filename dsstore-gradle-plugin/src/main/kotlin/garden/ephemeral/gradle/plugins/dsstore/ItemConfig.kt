package garden.ephemeral.gradle.plugins.dsstore

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

/**
 * Holder for configuration for a single item in the directory.
 */
abstract class ItemConfig {
    /**
     * Specifies the location of the icon, roughly from the top left of the Finder window.
     */
    @get:Nested
    @get:Optional
    abstract val iconLocation: Property<IconLocation>

    /**
     * Extension convenience method for DSL to set X and Y without explicitly
     * constructing the location object.
     *
     * @param x the X offset.
     * @param y the Y offset.
     */
    fun Property<IconLocation>.set(x: Int, y: Int) = set(IconLocation(x, y))

    data class IconLocation(
        @get:Input
        val x: Int,
        @get:Input
        val y: Int
    )
}
