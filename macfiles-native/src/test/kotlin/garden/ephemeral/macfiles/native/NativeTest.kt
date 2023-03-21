package garden.ephemeral.macfiles.native

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import garden.ephemeral.macfiles.alias.FileSystemType
import garden.ephemeral.macfiles.alias.Kind
import garden.ephemeral.macfiles.alias.VolumeType
import garden.ephemeral.macfiles.bookmark.BookmarkKeys
import garden.ephemeral.macfiles.bookmark.types.URL
import garden.ephemeral.macfiles.bookmark.types.UUID
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.common.types.FourCC
import java.io.File
import java.time.Instant
import kotlin.test.Test

class NativeTest {
    @Test
    fun `generating alias for a file`() {
        val alias = aliasForFile(File("build.gradle.kts"))
        assertThat(alias.appInfo).isEqualTo(FourCC.ZERO)
        assertThat(alias.version).isEqualTo(2)

        val volume = alias.volume
        assertThat(volume.name).isNotNull()
        assertThat(volume.creationDate).isNotNull()
        assertThat(volume.fsType).isEqualTo(FileSystemType.HFS_PLUS)
        assertThat(volume.diskType).isEqualTo(VolumeType.FIXED_DISK)
        assertThat(volume.attributeFlags).isEqualTo(0U)
        assertThat(volume.fsId).isEqualTo("\u0000\u0000")
        assertThat(volume.appleShareInfo).isNull()
        assertThat(volume.driverName).isNull()
        assertThat(volume.posixPath).isNotNull()
        assertThat(volume.diskImageAlias).isNull()
        assertThat(volume.dialupInfo).isNull()
        assertThat(volume.networkMountInfo).isNull()

        val target = alias.target
        assertThat(target.name).isEqualTo("build.gradle.kts")
        assertThat(target.kind).isEqualTo(Kind.FILE)
        assertThat(target.folderCnid).isNotNull()
        assertThat(target.cnid).isNotNull()
        assertThat(target.creationDate).isNotNull()
        assertThat(target.creatorCode).isEqualTo(FourCC.ZERO)
        assertThat(target.typeCode).isEqualTo(FourCC.ZERO)
        assertThat(target.levelsFrom).isEqualTo(-1)
        assertThat(target.levelsTo).isEqualTo(-1)
        assertThat(target.folderName).isEqualTo("macfiles-native")
        assertThat(target.cnidPath).isNotNull()
        assertThat(target.carbonPath).isNotNull().endsWith(":\u0000macfiles-native:\u0000build.gradle.kts")
        assertThat(target.posixPath).isNotNull().endsWith("/macfiles-native/build.gradle.kts")
        assertThat(target.userHomePrefixLen).isNull()
    }

    @Test
    fun `generating alias for a folder`() {
        val alias = aliasForFile(File("build"))
        assertThat(alias.appInfo).isEqualTo(FourCC.ZERO)
        assertThat(alias.version).isEqualTo(2)

        val volume = alias.volume
        assertThat(volume.name).isNotNull()
        assertThat(volume.creationDate).isNotNull()
        assertThat(volume.fsType).isEqualTo(FileSystemType.HFS_PLUS)
        assertThat(volume.diskType).isEqualTo(VolumeType.FIXED_DISK)
        assertThat(volume.attributeFlags).isEqualTo(0U)
        assertThat(volume.fsId).isEqualTo("\u0000\u0000")
        assertThat(volume.appleShareInfo).isNull()
        assertThat(volume.driverName).isNull()
        assertThat(volume.posixPath).isNotNull()
        assertThat(volume.diskImageAlias).isNull()
        assertThat(volume.dialupInfo).isNull()
        assertThat(volume.networkMountInfo).isNull()

        val target = alias.target
        assertThat(target.name).isEqualTo("build")
        assertThat(target.kind).isEqualTo(Kind.FOLDER)
        assertThat(target.folderCnid).isNotNull()
        assertThat(target.cnid).isNotNull()
        assertThat(target.creationDate).isNotNull()
        assertThat(target.creatorCode).isEqualTo(FourCC.ZERO)
        assertThat(target.typeCode).isEqualTo(FourCC.ZERO)
        assertThat(target.levelsFrom).isEqualTo(-1)
        assertThat(target.levelsTo).isEqualTo(-1)
        assertThat(target.folderName).isEqualTo("macfiles-native")
        assertThat(target.cnidPath).isNotNull()
        assertThat(target.carbonPath).isNotNull().endsWith(":\u0000macfiles-native:\u0000build")
        assertThat(target.posixPath).isNotNull().endsWith("/macfiles-native/build")
        assertThat(target.userHomePrefixLen).isNull()
    }

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
            .index(0).isNotNull().isInstanceOf(UInt::class)
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
            .index(0).isNotNull().isInstanceOf(UInt::class)
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
}
