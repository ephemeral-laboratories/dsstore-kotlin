package garden.ephemeral.gradle.plugins.dsstore

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*

/**
 * Task to create a `.DS_Store` file in the specified location.
 */
abstract class GenerateDSStore : DefaultTask() {
    /**
     * The file to write the `.DSStore` file into.
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /**
     * Holds configuration for the root directory.
     *
     * This method is public to allow Gradle to see the config, for up-to-date checks.
     * DSL users should not use this property directly, but should configure the task
     * using the DSL.
     */
    @get:Nested
    val rootItemConfig: RootItemConfig = project.objects.newInstance(RootItemConfig::class.java)

    /**
     * Holds configuration for each item in the directory.
     *
     * This method is public to allow Gradle to see the config, for up-to-date checks.
     * DSL users should not use this property directly, but should configure the task
     * using the DSL.
     */
    @get:Nested
    val itemConfigByName = mutableMapOf<String, ItemConfig>()

    /**
     * DSL for configuring the directory the .DS_Store file resides in.
     *
     * @param configBlock the block of logic containing the
     *        configuration for the root item.
     */
    fun root(configBlock: RootItemConfig.() -> Unit) {
        configBlock(rootItemConfig)
    }

    /**
     * DSL for configuring an item in the directory.
     *
     * @param name the name of the item to configure (its filename.)
     * @param configBlock the block of logic containing the configuration
     *        for the root item.
     */
    fun item(name: String, configBlock: ItemConfig.() -> Unit) {
        val itemConfig = itemConfigByName.getOrPut(name) {
            project.objects.newInstance(ItemConfig::class.java)
        }
        configBlock(itemConfig)
    }

    /**
     * Action Gradle calls to execute the task.
     */
    @TaskAction
    fun generate() {
        DSStoreGenerator.generate(
            outputFile.get().asFile.toPath(),
            rootItemConfig,
            itemConfigByName
        )
    }
}
