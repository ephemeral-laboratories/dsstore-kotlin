package garden.ephemeral.macfiles.native

import assertk.*
import assertk.assertions.*
import garden.ephemeral.macfiles.alias.*
import garden.ephemeral.macfiles.bookmark.*
import garden.ephemeral.macfiles.bookmark.types.*
import garden.ephemeral.macfiles.common.types.*
import org.junit.jupiter.api.condition.*
import java.io.*
import java.time.*
import kotlin.test.*

@EnabledOnOs(OS.MAC)
class NativeBookmarkTest {
    @Test
    fun `generating bookmark for a file`() {
        val bookmark = bookmarkForFile(File("build.gradle.kts"))
        assertThat(bookmark.tocs).hasSize(1)
        val toc = bookmark.tocs[1]!!
        assertThat(toc).hasSize(18)
        assertThat(toc[BookmarkKeys.kBookmarkPath]).isNotNull()
            .isInstanceOf(List::class)
            .endsWith("macfiles-native", "build.gradle.kts")
        assertThat(toc[BookmarkKeys.kBookmarkCNIDPath]).isNotNull().isInstanceOf(List::class)
            .index(0).isNotNull().isInstanceOf(Long::class)
        assertThat(toc[BookmarkKeys.kBookmarkFileCreationDate]).isNotNull().isInstanceOf(Instant::class)
        assertThat(toc[BookmarkKeys.kBookmarkFileProperties]).isEqualTo(
            Blob.fromHexDump("""
                01 00 00 00 00 00 00 00 0f 00 00 00 00 00 00 00
                00 00 00 00 00 00 00 00 
                """.trimIndent()
            )
        )
        assertThat(toc[BookmarkKeys.kBookmarkContainingFolder]).isNotNull().isInstanceOf(Int::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumePath]).isNotNull().isInstanceOf(String::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeIsRoot]).isEqualTo(false)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeURL]).isNotNull().isInstanceOf(URL.Absolute::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeName]).isNotNull().isInstanceOf(String::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeSize]).isNotNull().isInstanceOf(Long::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeCreationDate]).isNotNull().isInstanceOf(Instant::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeUUID]).isNotNull().isInstanceOf(UUID::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeProperties]).isEqualTo(
            Blob.fromHexDump("""
                81 00 00 00 01 00 00 00 ef 13 00 00 01 00 00 00
                00 00 00 00 00 00 00 00
                """.trimIndent())
        )
        assertThat(toc[BookmarkKeys.kBookmarkCreationOptions]).isEqualTo(512)
        assertThat(toc[BookmarkKeys.kBookmarkWasFileReference]).isEqualTo(true)
        assertThat(toc[BookmarkKeys.kBookmarkUserName]).isEqualTo("unknown")
        assertThat(toc[BookmarkKeys.kBookmarkUID]).isEqualTo(99)
        assertThat(toc[BookmarkKeys.kBookmarkURLLengths]).isNotNull().isInstanceOf(List::class).all {
            hasSize(2)
            index(0).isNotNull().isInstanceOf(Int::class)
            index(1).isNotNull().isInstanceOf(Int::class)
        }
    }

    @Test
    fun `generating bookmark for a folder`() {
        val bookmark = bookmarkForFile(File("build"))
        assertThat(bookmark.tocs).hasSize(1)
        val toc = bookmark.tocs[1]!!
        assertThat(toc).hasSize(18)
        assertThat(toc[BookmarkKeys.kBookmarkPath]).isNotNull()
            .isInstanceOf(List::class)
            .endsWith("macfiles-native", "build")
        assertThat(toc[BookmarkKeys.kBookmarkCNIDPath]).isNotNull().isInstanceOf(List::class)
            .index(0).isNotNull().isInstanceOf(Long::class)
        assertThat(toc[BookmarkKeys.kBookmarkFileCreationDate]).isNotNull().isInstanceOf(Instant::class)
        assertThat(toc[BookmarkKeys.kBookmarkFileProperties]).isEqualTo(
            Blob.fromHexDump("""
                02 00 00 00 00 00 00 00 0f 00 00 00 00 00 00 00
                00 00 00 00 00 00 00 00 
                """.trimIndent()
            )
        )
        assertThat(toc[BookmarkKeys.kBookmarkContainingFolder]).isNotNull().isInstanceOf(Int::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumePath]).isNotNull().isInstanceOf(String::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeIsRoot]).isEqualTo(false)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeURL]).isNotNull().isInstanceOf(URL.Absolute::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeName]).isNotNull().isInstanceOf(String::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeSize]).isNotNull().isInstanceOf(Long::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeCreationDate]).isNotNull().isInstanceOf(Instant::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeUUID]).isNotNull().isInstanceOf(UUID::class)
        assertThat(toc[BookmarkKeys.kBookmarkVolumeProperties]).isEqualTo(
            Blob.fromHexDump("""
                81 00 00 00 01 00 00 00 ef 13 00 00 01 00 00 00
                00 00 00 00 00 00 00 00
                """.trimIndent())
        )
        assertThat(toc[BookmarkKeys.kBookmarkCreationOptions]).isEqualTo(512)
        assertThat(toc[BookmarkKeys.kBookmarkWasFileReference]).isEqualTo(true)
        assertThat(toc[BookmarkKeys.kBookmarkUserName]).isEqualTo("unknown")
        assertThat(toc[BookmarkKeys.kBookmarkUID]).isEqualTo(99)
        assertThat(toc[BookmarkKeys.kBookmarkURLLengths]).isNotNull().isInstanceOf(List::class).all {
            hasSize(2)
            index(0).isNotNull().isInstanceOf(Int::class)
            index(1).isNotNull().isInstanceOf(Int::class)
        }
    }

    @Test
    fun `generating a bookmark for a non-existent file`() {
        assertThat {
            bookmarkForFile(File("does-not-exist.txt"))
        }.isFailure()
            .isInstanceOf(NoSuchFileException::class)
            .prop(NoSuchFileException::file)
            .isEqualTo(File("does-not-exist.txt"))
    }
}
