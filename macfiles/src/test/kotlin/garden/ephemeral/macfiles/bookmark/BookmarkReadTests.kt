package garden.ephemeral.macfiles.bookmark

import assertk.assertThat
import assertk.assertions.*
import garden.ephemeral.macfiles.bookmark.types.URL
import garden.ephemeral.macfiles.common.io.Block
import garden.ephemeral.macfiles.common.types.Blob
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

class BookmarkReadTests {
    @Test
    fun `bookmark to a file`() {
        val bookmark = readFromFile("file.bookmark")
        assertThat(bookmark.tocs).hasSize(1)
        val toc = bookmark.tocs[1]!!
        assertThat(toc).containsAll(
            StandardKeys.kBookmarkPath to listOf("..", "..", "..", "Users", "trejkaz", "jenkins.log"),
            StandardKeys.kBookmarkCNIDPath to listOf<Number>(
                1152921500312727487L,
                1152921500311879701L,
                2,
                23829,
                301088,
                3244388
            ),
            StandardKeys.kBookmarkFileProperties to Blob(
                byteArrayOf(
                    0x1, 0, 0, 0, 0, 0, 0, 0, 0xf, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0
                )
            ),
            StandardKeys.kBookmarkFileCreationDate to Instant.parse("2022-09-01T23:04:01.204694986Z"),
            StandardKeys.kBookmarkVolumePath to "/System/Volumes/Data",
            StandardKeys.kBookmarkVolumeURL to URL.Absolute("file:///System/Volumes/Data"),
            StandardKeys.kBookmarkVolumeName to "Data",
            StandardKeys.kBookmarkVolumeUUID to "7053101F-22D5-4135-AE6F-DB2ECBC57613",
            StandardKeys.kBookmarkVolumeSize to 994662584320,
            StandardKeys.kBookmarkVolumeCreationDate to Instant.parse("2022-08-04T23:59:05.734845995Z"),
            StandardKeys.kBookmarkVolumeProperties to Blob(
                byteArrayOf(
                    0x81.toByte(), 0, 0, 0, 0x1, 0, 0, 0, 0xef.toByte(), 0x13, 0, 0, 0x1, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0
                )
            ),
            StandardKeys.kBookmarkVolumeIsRoot to false,
            StandardKeys.kBookmarkContainingFolder to 4,
            StandardKeys.kBookmarkUserName to "unknown",
            StandardKeys.kBookmarkUID to 99,
            StandardKeys.kBookmarkWasFileReference to true,
            StandardKeys.kBookmarkCreationOptions to 512
        )
    }

    @Test
    fun `bookmark to a folder`() {
        val bookmark = readFromFile("folder.bookmark")
        assertThat(bookmark.tocs).hasSize(1)
        val toc = bookmark.tocs[1]!!
        assertThat(toc).containsAll(
            StandardKeys.kBookmarkPath to listOf("..", "..", "..", "Applications"),
            StandardKeys.kBookmarkCNIDPath to listOf<Number>(
                1152921500312727487L, 1152921500311879701L, 2, 23833
            ),
            StandardKeys.kBookmarkFileProperties to Blob(
                byteArrayOf(
                    0x2, 0, 0, 0, 0, 0, 0, 0, 0xf, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0
                )
            ),
            StandardKeys.kBookmarkFileCreationDate to Instant.parse("2022-07-14T09:04:26.314218997Z"),
            StandardKeys.kBookmarkVolumePath to "/System/Volumes/Data",
            StandardKeys.kBookmarkVolumeURL to URL.Absolute("file:///System/Volumes/Data"),
            StandardKeys.kBookmarkVolumeName to "Data",
            StandardKeys.kBookmarkVolumeUUID to "7053101F-22D5-4135-AE6F-DB2ECBC57613",
            StandardKeys.kBookmarkVolumeSize to 994662584320,
            StandardKeys.kBookmarkVolumeCreationDate to Instant.parse("2022-08-04T23:59:05.734845995Z"),
            StandardKeys.kBookmarkVolumeProperties to Blob(
                byteArrayOf(
                    0x81.toByte(), 0, 0, 0, 0x1, 0, 0, 0, 0xef.toByte(), 0x13, 0, 0, 0x1, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0
                )
            ),
            StandardKeys.kBookmarkVolumeIsRoot to false,
            StandardKeys.kBookmarkContainingFolder to 2,
            StandardKeys.kBookmarkUserName to "unknown",
            StandardKeys.kBookmarkUID to 99,
            StandardKeys.kBookmarkWasFileReference to true,
            StandardKeys.kBookmarkCreationOptions to 512
        )
    }

    @Test
    fun `bookmark to an app`() {
        val bookmark = readFromFile("app.bookmark")
        assertThat(bookmark.tocs).hasSize(1)
        val toc = bookmark.tocs[1]!!
        assertThat(toc).containsAll(
            StandardKeys.kBookmarkPath to listOf("..", "..", "..", "Applications", "Safari.app"),
            StandardKeys.kBookmarkCNIDPath to listOf<Number>(
                1152921500312727487L, 1152921500311879701L, 2, 23833, 25880792
            ),
            StandardKeys.kBookmarkFileProperties to Blob(
                byteArrayOf(
                    0x2, 0, 0, 0, 0, 0, 0, 0, 0xf, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0
                )
            ),
            StandardKeys.kBookmarkFileCreationDate to Instant.parse("2022-10-21T01:39:29Z"),
            StandardKeys.kBookmarkVolumePath to "/System/Volumes/Data",
            StandardKeys.kBookmarkVolumeURL to URL.Absolute("file:///System/Volumes/Data"),
            StandardKeys.kBookmarkVolumeName to "Data",
            StandardKeys.kBookmarkVolumeUUID to "7053101F-22D5-4135-AE6F-DB2ECBC57613",
            StandardKeys.kBookmarkVolumeSize to 994662584320,
            StandardKeys.kBookmarkVolumeCreationDate to Instant.parse("2022-08-04T23:59:05.734845995Z"),
            StandardKeys.kBookmarkVolumeProperties to Blob(
                byteArrayOf(
                    0x81.toByte(), 0, 0, 0, 0x1, 0, 0, 0, 0xef.toByte(), 0x13, 0, 0, 0x1, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0
                )
            ),
            StandardKeys.kBookmarkVolumeIsRoot to false,
            StandardKeys.kBookmarkContainingFolder to 3,
            StandardKeys.kBookmarkUserName to "unknown",
            StandardKeys.kBookmarkUID to 99,
            StandardKeys.kBookmarkWasFileReference to true,
            StandardKeys.kBookmarkCreationOptions to 512
        )
    }

    @Test
    fun `pending alias file created by macOS Monterey`() {
        // Asserting that parsing actual on-disk aliases does _not_ yet work.
        assertThat {
            val bookmark = readFromFile("Safari alias.bookmark")
            assertThat(bookmark).isEqualTo("nope")
        }.isFailure()
            .isInstanceOf(IllegalArgumentException::class)
            .hasMessage("Not a bookmark file (header size too short)")
    }

    private fun readFromFile(filename: String): Bookmark {
        // Asserting that parsing actual on-disk aliases does _not_ work.
        val path = Path.of("src/test/resources/garden/ephemeral/macfiles/bookmark/$filename")
        val buffer = ByteBuffer.wrap(Files.readAllBytes(path))
        val block = Block(buffer)
        return Bookmark.readFrom(block)
    }
}