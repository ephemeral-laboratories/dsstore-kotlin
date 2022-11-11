package util

import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption

/**
 * Enumeration of available modes when opening a file.
 */
enum class FileMode {
    /**
     * Opens the file read-only.
     */
    READ_ONLY {
        override fun toOpenOptions(): Set<OpenOption> {
            return setOf(StandardOpenOption.READ)
        }
    },

    /**
     * Opens the file read-write. Will create the file if it didn't already exist.
     */
    READ_WRITE {
        override fun toOpenOptions(): Set<OpenOption> {
            return setOf(StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)
        }
    };

    /**
     * Converts the mode to a set of [OpenOption] for use with NIO APIs.
     *
     * @return the open options.
     */
    abstract fun toOpenOptions(): Set<OpenOption>
}