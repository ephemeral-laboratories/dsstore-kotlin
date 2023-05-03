package garden.ephemeral.gradle.plugins.dsstore

import org.gradle.api.*
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import javax.inject.Inject

/**
 * Task to create a `.DS_Store` file in the specified location.
 */
abstract class GenerateDSStore : DefaultTask(), DSStoreGenerator {
    // For some reason this needs to be the concrete class.
    // Gradle reflects on the declared return type to determine its properties,
    // rather than using whatever is present at runtime.
    @Nested
    val generator: DefaultDSStoreGenerator = project.objects.newInstance(DefaultDSStoreGenerator::class.java)

    @get:Internal
    override val outputFile: RegularFileProperty by generator::outputFile

    override fun root(configBlock: RootItemConfig.() -> Unit) = generator.root(configBlock)

    override fun item(name: String, configBlock: ItemConfig.() -> Unit) = generator.item(name, configBlock)

    @TaskAction
    override fun generate() = generator.generate()
}

