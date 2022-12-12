package garden.ephemeral.macfiles.alias

import assertk.assertThat
import assertk.assertions.isEqualTo
import garden.ephemeral.macfiles.common.io.ByteBufferDataInput
import garden.ephemeral.macfiles.common.types.FourCC
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

class AliasReadTests {
    // Note: Test alias files were created with Python implementation, so it's unknown
    // whether the values contained within match what would be in a true alias file.
    // Alias files are a deprecated format which there is no obvious way to create
    // in macOS anymore.

    @Test
    fun `alias to file`() {
        val alias = readFromFile("file.alias")
        assertThat(alias).isEqualTo(
            Alias(
                appInfo = FourCC.ZERO,
                version = 2,
                volumeInfo = VolumeInfo(
                    name = "Mac Test",
                    creationDate = Instant.parse("2022-12-12T23:13:02Z"),
                    fsType = "H+",
                    diskType = VolumeType.FIXED_DISK,
                    attributeFlags = 0U,
                    fsId = "\u0000\u0000",
                ),
                targetInfo = TargetInfo(
                    kind = Kind.FILE,
                    filename = "Test file.txt",
                    folderCnid = 2U,
                    cnid = 147U,
                    creationDate = Instant.parse("2022-12-12T23:13:54Z"),
                    creatorCode = FourCC.ZERO,
                    typeCode = FourCC.ZERO,
                    folderName = "Mac Test",
                    carbonPath = "Mac Test:Test file.txt",
                    posixPath = "/Volumes/Mac Test"
                )
            )
        )
    }

    @Test
    fun `alias to folder`() {
        val alias = readFromFile("folder.alias")
        assertThat(alias).isEqualTo(
            Alias(
                appInfo = FourCC.ZERO,
                version = 2,
                volumeInfo = VolumeInfo(
                    name = "Mac Test",
                    creationDate = Instant.parse("2022-12-12T23:13:02Z"),
                    fsType = "H+",
                    diskType = VolumeType.FIXED_DISK,
                    attributeFlags = 0U,
                    fsId = "\u0000\u0000",
                ),
                targetInfo = TargetInfo(
                    kind = Kind.FOLDER,
                    filename = "Test folder",
                    folderCnid = 2U,
                    cnid = 143U,
                    creationDate = Instant.parse("2022-12-12T23:13:23Z"),
                    creatorCode = FourCC.ZERO,
                    typeCode = FourCC.ZERO,
                    folderName = "Mac Test",
                    carbonPath = "Mac Test:Test folder",
                    posixPath = "/Volumes/Mac Test"
                )
            )
        )
    }

    @Test
    fun `alias to volume`() {
        val alias = readFromFile("volume.alias")
        assertThat(alias).isEqualTo(
            Alias(
                appInfo = FourCC.ZERO,
                version = 2,
                volumeInfo = VolumeInfo(
                    name = "Mac Test",
                    creationDate = Instant.parse("2022-12-12T23:13:02Z"),
                    fsType = "H+",
                    diskType = VolumeType.FIXED_DISK,
                    attributeFlags = 0U,
                    fsId = "\u0000\u0000",
                ),
                targetInfo = TargetInfo(
                    kind = Kind.FOLDER,
                    filename = "Mac Test",
                    folderCnid = 1U,
                    cnid = 2U,
                    creationDate = Instant.parse("2022-12-12T23:13:02Z"),
                    creatorCode = FourCC.ZERO,
                    typeCode = FourCC.ZERO,
                    folderName = "Volumes",
                    carbonPath = "Mac Test:.",
                    posixPath = "/Volumes/Mac Test"
                )
            )
        )
    }

    private fun readFromFile(filename: String): Alias {
        val path = Path.of("src/test/resources/garden/ephemeral/macfiles/alias/$filename")
        val buffer = ByteBuffer.wrap(Files.readAllBytes(path))
        val dataInput = ByteBufferDataInput(buffer)
        return Alias.readFrom(dataInput)
    }
}