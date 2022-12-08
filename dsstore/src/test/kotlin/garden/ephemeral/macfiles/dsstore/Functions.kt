package garden.ephemeral.macfiles.dsstore

import java.nio.file.Path

fun getFilePath(filename: String): Path =
    Path.of("src/test/resources/garden/ephemeral/macfiles/dsstore/$filename")
