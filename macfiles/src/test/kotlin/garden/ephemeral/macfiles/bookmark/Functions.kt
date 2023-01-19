package garden.ephemeral.macfiles.bookmark

import java.nio.file.Path

fun getFilePath(filename: String): Path =
    Path.of("src/test/resources/garden/ephemeral/macfiles/bookmark/$filename")
