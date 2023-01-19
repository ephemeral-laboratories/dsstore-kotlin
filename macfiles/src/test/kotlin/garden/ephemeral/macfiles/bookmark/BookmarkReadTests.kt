package garden.ephemeral.macfiles.bookmark

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import garden.ephemeral.macfiles.bookmark.types.URL
import garden.ephemeral.macfiles.common.io.Block
import garden.ephemeral.macfiles.common.types.Blob
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.file.Files
import java.time.Instant

class BookmarkReadTests {
    @Test
    fun `bookmark to a file`() {
        val bookmark = readFromFile("file.bookmark")
        assertThat(bookmark).isEqualTo(
            EXPECTED_FILE_BOOKMARK
        )
    }

    @Test
    fun `bookmark to a folder`() {
        val bookmark = readFromFile("folder.bookmark")
        assertThat(bookmark).isEqualTo(
            Bookmark.build {
                put(BookmarkKeys.kBookmarkPath, listOf("..", "..", "..", "Applications"))
                put(
                    BookmarkKeys.kBookmarkCNIDPath, listOf<Number>(
                        1152921500312727487L, 1152921500311879701L, 2, 23833
                    )
                )
                put(
                    BookmarkKeys.kBookmarkFileProperties, Blob(
                        byteArrayOf(
                            0x2, 0, 0, 0, 0, 0, 0, 0, 0xf, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0
                        )
                    )
                )
                put(BookmarkKeys.kBookmarkFileCreationDate, Instant.parse("2022-07-14T09:04:26.314218997Z"))
                put(BookmarkKeys.kBookmarkVolumePath, "/System/Volumes/Data")
                put(BookmarkKeys.kBookmarkVolumeURL, URL.Absolute("file:///System/Volumes/Data"))
                put(BookmarkKeys.kBookmarkVolumeName, "Data")
                put(BookmarkKeys.kBookmarkVolumeUUID, "7053101F-22D5-4135-AE6F-DB2ECBC57613")
                put(BookmarkKeys.kBookmarkVolumeSize, 994662584320)
                put(BookmarkKeys.kBookmarkVolumeCreationDate, Instant.parse("2022-08-04T23:59:05.734845995Z"))
                put(
                    BookmarkKeys.kBookmarkVolumeProperties, Blob(
                        byteArrayOf(
                            0x81.toByte(), 0, 0, 0, 0x1, 0, 0, 0, 0xef.toByte(), 0x13, 0, 0, 0x1, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0
                        )
                    )
                )
                put(BookmarkKeys.kBookmarkVolumeIsRoot, false)
                put(BookmarkKeys.kBookmarkContainingFolder, 2)
                put(BookmarkKeys.kBookmarkUserName, "unknown")
                put(BookmarkKeys.kBookmarkUID, 99)
                put(BookmarkKeys.kBookmarkWasFileReference, true)
                put(BookmarkKeys.kBookmarkCreationOptions, 512)
            }
        )
    }

    @Test
    fun `bookmark to an app`() {
        val bookmark = readFromFile("app.bookmark")
        assertThat(bookmark).isEqualTo(
            Bookmark.build {
                put(BookmarkKeys.kBookmarkPath, listOf("..", "..", "..", "Applications", "Safari.app"))
                put(
                    BookmarkKeys.kBookmarkCNIDPath, listOf<Number>(
                        1152921500312727487L, 1152921500311879701L, 2, 23833, 25880792
                    )
                )
                put(
                    BookmarkKeys.kBookmarkFileProperties, Blob(
                        byteArrayOf(
                            0x2, 0, 0, 0, 0, 0, 0, 0, 0xf, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0
                        )
                    )
                )
                put(BookmarkKeys.kBookmarkFileCreationDate, Instant.parse("2022-10-21T01:39:29Z"))
                put(BookmarkKeys.kBookmarkVolumePath, "/System/Volumes/Data")
                put(BookmarkKeys.kBookmarkVolumeURL, URL.Absolute("file:///System/Volumes/Data"))
                put(BookmarkKeys.kBookmarkVolumeName, "Data")
                put(BookmarkKeys.kBookmarkVolumeUUID, "7053101F-22D5-4135-AE6F-DB2ECBC57613")
                put(BookmarkKeys.kBookmarkVolumeSize, 994662584320)
                put(BookmarkKeys.kBookmarkVolumeCreationDate, Instant.parse("2022-08-04T23:59:05.734845995Z"))
                put(
                    BookmarkKeys.kBookmarkVolumeProperties, Blob(
                        byteArrayOf(
                            0x81.toByte(), 0, 0, 0, 0x1, 0, 0, 0, 0xef.toByte(), 0x13, 0, 0, 0x1, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0
                        )
                    )
                )
                put(BookmarkKeys.kBookmarkVolumeIsRoot, false)
                put(BookmarkKeys.kBookmarkContainingFolder, 3)
                put(BookmarkKeys.kBookmarkUserName, "unknown")
                put(BookmarkKeys.kBookmarkUID, 99)
                put(BookmarkKeys.kBookmarkWasFileReference, true)
                put(BookmarkKeys.kBookmarkCreationOptions, 512)
            }
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
        val path = getFilePath(filename)
        val buffer = ByteBuffer.wrap(Files.readAllBytes(path))
        val block = Block(buffer)
        return Bookmark.readFrom(block)
    }

    companion object {
        val EXPECTED_FILE_BOOKMARK = Bookmark.build {
            put(BookmarkKeys.kBookmarkPath, listOf("..", "..", "..", "Users", "trejkaz", "jenkins.log"))
            put(
                BookmarkKeys.kBookmarkCNIDPath, listOf<Number>(
                    1152921500312727487L,
                    1152921500311879701L,
                    2,
                    23829,
                    301088,
                    3244388
                )
            )
            put(
                BookmarkKeys.kBookmarkFileProperties, Blob(
                    byteArrayOf(
                        0x1, 0, 0, 0, 0, 0, 0, 0, 0xf, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0
                    )
                )
            )
            put(BookmarkKeys.kBookmarkFileCreationDate, Instant.parse("2022-09-01T23:04:01.204694986Z"))
            put(BookmarkKeys.kBookmarkVolumePath, "/System/Volumes/Data")
            put(BookmarkKeys.kBookmarkVolumeURL, URL.Absolute("file:///System/Volumes/Data"))
            put(BookmarkKeys.kBookmarkVolumeName, "Data")
            put(BookmarkKeys.kBookmarkVolumeUUID, "7053101F-22D5-4135-AE6F-DB2ECBC57613")
            put(BookmarkKeys.kBookmarkVolumeSize, 994662584320)
            put(BookmarkKeys.kBookmarkVolumeCreationDate, Instant.parse("2022-08-04T23:59:05.734845995Z"))
            put(
                BookmarkKeys.kBookmarkVolumeProperties, Blob(
                    byteArrayOf(
                        0x81.toByte(), 0, 0, 0, 0x1, 0, 0, 0, 0xef.toByte(), 0x13, 0, 0, 0x1, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0
                    )
                )
            )
            put(BookmarkKeys.kBookmarkVolumeIsRoot, false)
            put(BookmarkKeys.kBookmarkContainingFolder, 4)
            put(BookmarkKeys.kBookmarkUserName, "unknown")
            put(BookmarkKeys.kBookmarkUID, 99)
            put(BookmarkKeys.kBookmarkWasFileReference, true)
            put(BookmarkKeys.kBookmarkCreationOptions, 512)
        }
    }
}