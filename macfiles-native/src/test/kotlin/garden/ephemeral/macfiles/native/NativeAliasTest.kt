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
class NativeAliasTest {
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
    fun `generating an alias for a non-existent file`() {
        assertThat {
            aliasForFile(File("does-not-exist.txt"))
        }.isFailure()
            .isInstanceOf(NoSuchFileException::class)
            .prop(NoSuchFileException::file)
            .isEqualTo(File("does-not-exist.txt"))
    }
}
