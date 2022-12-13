package garden.ephemeral.macfiles.alias

import assertk.assertThat
import assertk.assertions.isEqualTo
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.common.types.FourCC
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

class AliasWriteTests {
    @Test
    fun `reproducing an existing file`() {
        val alias = Alias(
            appInfo = FourCC.ZERO,
            version = 2,
            volume = VolumeInfo(
                name = "Mac Test",
                creationDate = Instant.parse("2022-12-12T23:13:02Z"),
                fsType = "H+",
                diskType = VolumeType.FIXED_DISK,
                attributeFlags = 0U,
                fsId = "\u0000\u0000",
                posixPath = "/Volumes/Mac Test"
            ),
            target = TargetInfo(
                name = "Test file.txt",
                kind = Kind.FILE,
                folderCnid = 2U,
                cnid = 147U,
                creationDate = Instant.parse("2022-12-12T23:13:54Z"),
                creatorCode = FourCC.ZERO,
                typeCode = FourCC.ZERO,
                folderName = "Mac Test",
                carbonPath = "Mac Test:Test file.txt",
                posixPath = "/Test file.txt"
            )
        )
        val actual = alias.toBlob()

        val path = Path.of("src/test/resources/garden/ephemeral/macfiles/alias/file.alias")
        val expected = Blob(Files.readAllBytes(path))

        assertThat(actual).isEqualTo(expected)
    }
}
