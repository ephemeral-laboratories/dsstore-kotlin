package garden.ephemeral.gradle.plugins.dsstore

import org.gradle.api.Plugin
import org.gradle.api.Project

class DSStorePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Nothing to do here.
        // We can't really preconfigure any tasks, because we don't know what
        // the build script will want.
    }
}