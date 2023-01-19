package garden.ephemeral.macfiles.alias

import java.nio.file.Path

fun getFilePath(filename: String): Path =
    Path.of("src/test/resources/garden/ephemeral/macfiles/alias/$filename")
