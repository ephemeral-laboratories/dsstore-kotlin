package garden.ephemeral.gradle.plugins.dsstore

import org.gradle.api.*
import org.gradle.nativeplatform.platform.internal.*

class DSStorePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // The task only works on macOS because of all the native routines it uses to fetch
        // information about files.
        project.tasks.withType(GenerateDSStore::class.java) { task ->
            task.onlyIf {
                DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX
            }
        }
    }
}