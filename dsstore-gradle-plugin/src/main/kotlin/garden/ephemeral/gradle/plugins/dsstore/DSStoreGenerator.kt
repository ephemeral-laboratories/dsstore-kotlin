package garden.ephemeral.gradle.plugins.dsstore

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Interface to the DS_Store generator.
 */
interface DSStoreGenerator {
    /**
     * The file to write the `.DSStore` file into.
     */
    val outputFile: RegularFileProperty

    /**
     * DSL for configuring the directory the .DS_Store file resides in.
     *
     * @param configBlock the block of logic containing the
     *        configuration for the root item.
     */
    fun root(configBlock: RootItemConfig.() -> Unit)

    /**
     * DSL for configuring an item in the directory.
     *
     * @param name the name of the item to configure (its filename.)
     * @param configBlock the block of logic containing the configuration
     *        for the root item.
     */
    fun item(name: String, configBlock: ItemConfig.() -> Unit)

    /**
     * Generates the .DS_Store file.
     */
    fun generate()
}