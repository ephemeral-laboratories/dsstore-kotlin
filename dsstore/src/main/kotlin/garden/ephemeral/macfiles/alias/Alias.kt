package garden.ephemeral.macfiles.alias

import garden.ephemeral.macfiles.common.MacTimeUtils
import garden.ephemeral.macfiles.common.io.DataInput
import garden.ephemeral.macfiles.common.io.DataOutput
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.common.types.FourCC
import java.nio.charset.StandardCharsets

/**
 * Representation of a macOS Alias file.
 *
 * @param appInfo application-specific information.
 * @param version the alias format version (we support versions 2 and 3).
 * @param volumeInfo info about the target's volume.
 * @param targetInfo info about the target.
 * @param unrecognised a list of extra tags we didn't recognise.
 */
data class Alias(
    val appInfo: FourCC,
    val version: Short,
    val volumeInfo: VolumeInfo?,
    val targetInfo: TargetInfo?,
    val unrecognised: List<Pair<Short, Blob>> = listOf()
) {
    fun writeTo(stream: DataOutput) {
        TODO("Write support")
        /*
        # We'll come back and fix the length when we're done
        pos = b.tell()
        b.write(struct.pack(b">4shh", self.appinfo, 0, self.version))

        carbon_volname = encode_utf8(self.volume.name).replace(b":", b"/")
        carbon_filename = encode_utf8(self.target.filename).replace(b":", b"/")
        voldate = (self.volume.creation_date - mac_epoch).total_seconds()
        crdate = (self.target.creation_date - mac_epoch).total_seconds()

        if self.version == 2:
            # NOTE: crdate should be in local time, but that's system dependent
            #       (so doing so is ridiculous, and nothing could rely on it).
            b.write(
                struct.pack(
                    b">h28pI2shI64pII4s4shhI2s10s",
                    self.target.kind,  # h
                    carbon_volname,  # 28p
                    int(voldate),  # I
                    self.volume.fs_type,  # 2s
                    self.volume.disk_type,  # h
                    self.target.folder_cnid,  # I
                    carbon_filename,  # 64p
                    self.target.cnid,  # I
                    int(crdate),  # I
                    self.target.creator_code,  # 4s
                    self.target.type_code,  # 4s
                    self.target.levels_from,  # h
                    self.target.levels_to,  # h
                    self.volume.attribute_flags,  # I
                    self.volume.fs_id,  # 2s
                    b"\0" * 10,  # 10s
                )
            )
        else:
            b.write(
                struct.pack(
                    b">hQ4shIIQI14s",
                    self.target.kind,  # h
                    int(voldate * 65536),  # Q
                    self.volume.fs_type,  # 4s
                    self.volume.disk_type,  # h
                    self.target.folder_cnid,  # I
                    self.target.cnid,  # I
                    int(crdate * 65536),  # Q
                    self.volume.attribute_flags,  # I
                    b"\0" * 14,  # 14s
                )
            )

        # Excuse the odd order; we're copying Finder
        if self.target.folder_name:
            carbon_foldername = encode_utf8(self.target.folder_name).replace(b":", b"/")
            b.write(struct.pack(b">hh", TAG_CARBON_FOLDER_NAME, len(carbon_foldername)))
            b.write(carbon_foldername)
            if len(carbon_foldername) & 1:
                b.write(b"\0")

        b.write(
            struct.pack(
                b">hhQhhQ",
                TAG_HIGH_RES_VOLUME_CREATION_DATE,
                8,
                int(voldate * 65536),
                TAG_HIGH_RES_CREATION_DATE,
                8,
                int(crdate * 65536),
            )
        )

        if self.target.cnid_path:
            cnid_path = struct.pack(
                ">%uI" % len(self.target.cnid_path), *self.target.cnid_path
            )
            b.write(struct.pack(b">hh", TAG_CNID_PATH, len(cnid_path)))
            b.write(cnid_path)

        if self.target.carbon_path:
            carbon_path = encode_utf8(self.target.carbon_path)
            b.write(struct.pack(b">hh", TAG_CARBON_PATH, len(carbon_path)))
            b.write(carbon_path)
            if len(carbon_path) & 1:
                b.write(b"\0")

        if self.volume.appleshare_info:
            ai = self.volume.appleshare_info
            if ai.zone:
                b.write(struct.pack(b">hh", TAG_APPLESHARE_ZONE, len(ai.zone)))
                b.write(ai.zone)
                if len(ai.zone) & 1:
                    b.write(b"\0")
            if ai.server:
                b.write(struct.pack(b">hh", TAG_APPLESHARE_SERVER_NAME, len(ai.server)))
                b.write(ai.server)
                if len(ai.server) & 1:
                    b.write(b"\0")
            if ai.username:
                b.write(struct.pack(b">hh", TAG_APPLESHARE_USERNAME, len(ai.username)))
                b.write(ai.username)
                if len(ai.username) & 1:
                    b.write(b"\0")

        if self.volume.driver_name:
            driver_name = encode_utf8(self.volume.driver_name)
            b.write(struct.pack(b">hh", TAG_DRIVER_NAME, len(driver_name)))
            b.write(driver_name)
            if len(driver_name) & 1:
                b.write(b"\0")

        if self.volume.network_mount_info:
            b.write(
                struct.pack(
                    b">hh", TAG_NETWORK_MOUNT_INFO, len(self.volume.network_mount_info)
                )
            )
            b.write(self.volume.network_mount_info)
            if len(self.volume.network_mount_info) & 1:
                b.write(b"\0")

        if self.volume.dialup_info:
            b.write(
                struct.pack(
                    b">hh", TAG_DIALUP_INFO, len(self.volume.network_mount_info)
                )
            )
            b.write(self.volume.network_mount_info)
            if len(self.volume.network_mount_info) & 1:
                b.write(b"\0")

        utf16 = decode_utf8(self.target.filename).replace(":", "/").encode("utf-16-be")
        b.write(
            struct.pack(b">hhh", TAG_UNICODE_FILENAME, len(utf16) + 2, len(utf16) // 2)
        )
        b.write(utf16)

        utf16 = decode_utf8(self.volume.name).replace(":", "/").encode("utf-16-be")
        b.write(
            struct.pack(
                b">hhh", TAG_UNICODE_VOLUME_NAME, len(utf16) + 2, len(utf16) // 2
            )
        )
        b.write(utf16)

        if self.target.posix_path:
            posix_path = encode_utf8(self.target.posix_path)
            b.write(struct.pack(b">hh", TAG_POSIX_PATH, len(posix_path)))
            b.write(posix_path)
            if len(posix_path) & 1:
                b.write(b"\0")

        if self.volume.posix_path:
            posix_path = encode_utf8(self.volume.posix_path)
            b.write(struct.pack(b">hh", TAG_POSIX_PATH_TO_MOUNTPOINT, len(posix_path)))
            b.write(posix_path)
            if len(posix_path) & 1:
                b.write(b"\0")

        if self.volume.disk_image_alias:
            d = self.volume.disk_image_alias.to_bytes()
            b.write(struct.pack(b">hh", TAG_RECURSIVE_ALIAS_OF_DISK_IMAGE, len(d)))
            b.write(d)
            if len(d) & 1:
                b.write(b"\0")

        if self.target.user_home_prefix_len is not None:
            b.write(
                struct.pack(
                    b">hhh",
                    TAG_USER_HOME_LENGTH_PREFIX,
                    2,
                    self.target.user_home_prefix_len,
                )
            )

        for t, v in self.extra:
            b.write(struct.pack(b">hh", t, len(v)))
            b.write(v)
            if len(v) & 1:
                b.write(b"\0")

        b.write(struct.pack(b">hh", -1, 0))

        blen = b.tell() - pos
        b.seek(pos + 4, os.SEEK_SET)
        b.write(struct.pack(b">h", blen))


     */
    }

    companion object {
        fun readFrom(stream: DataInput): Alias {
            val (appInfo, recSize, version) = AliasHeader.readFrom(stream)

            require(recSize >= 150) { "Incorrect alias length: $recSize" }
            require(version in 2..3) { "Unsupported alias version $version" }

            val record = if (version == 2.toShort()) {
                AliasRecordV2.readFrom(stream)
            } else {
                AliasRecordV3.readFrom(stream)
            }

            val alias = Builder(
                appInfo = appInfo,
                version = version,
                volumeInfo = record.deriveVolumeInfo(),
                targetInfo = record.deriveTargetInfo(),
            )

            var tagValue = stream.readShort()
            while (tagValue != (-1).toShort()) {
                val length = stream.readShort().toInt()
                when (Tag.findForValue(tagValue)) {
                    Tag.CARBON_FOLDER_NAME -> alias.targetInfo.folderName =
                        stream.readString(length, StandardCharsets.UTF_8).replace("/", ":")
                    Tag.CNID_PATH -> alias.targetInfo.cnidPath =
                        (0..(length / 4)).map { stream.readInt().toUInt() }
                    Tag.CARBON_PATH -> alias.targetInfo.carbonPath =
                        stream.readString(length, StandardCharsets.UTF_8)
                    Tag.APPLESHARE_ZONE -> alias.volumeInfo.lazyAppleShareInfo().zone =
                        stream.readString(length, StandardCharsets.UTF_8)
                    Tag.APPLESHARE_SERVER_NAME -> alias.volumeInfo.lazyAppleShareInfo().server =
                        stream.readString(length, StandardCharsets.UTF_8)
                    Tag.APPLESHARE_USERNAME -> alias.volumeInfo.lazyAppleShareInfo().user =
                        stream.readString(length, StandardCharsets.UTF_8)
                    Tag.DRIVER_NAME -> alias.volumeInfo.driverName =
                        stream.readString(length, StandardCharsets.UTF_8)
                    Tag.NETWORK_MOUNT_INFO -> alias.volumeInfo.networkMountInfo = stream.readBlob(length)
                    Tag.DIALUP_INFO -> alias.volumeInfo.dialupInfo = stream.readBlob(length)
                    Tag.UNICODE_FILENAME -> {
                        stream.skip(2)
                        alias.targetInfo.name = stream.readString(length - 2, StandardCharsets.UTF_16BE)
                    }
                    Tag.UNICODE_VOLUME_NAME -> {
                        stream.skip(2)
                        alias.volumeInfo.name = stream.readString(length - 2, StandardCharsets.UTF_16BE)
                    }
                    Tag.HIGH_RES_VOLUME_CREATION_DATE -> alias.volumeInfo.creationDate =
                        MacTimeUtils.decodeHighResInstant(stream.readLong())
                    Tag.HIGH_RES_CREATION_DATE -> alias.targetInfo.creationDate =
                        MacTimeUtils.decodeHighResInstant(stream.readLong())
                    Tag.POSIX_PATH,
                    Tag.POSIX_PATH_TO_MOUNTPOINT -> alias.targetInfo.posixPath =
                        stream.readString(length, StandardCharsets.UTF_8)
                    Tag.RECURSIVE_ALIAS_OF_DISK_IMAGE -> alias.volumeInfo.diskImageAlias =
                        readFrom(stream.readBlob(length).toBlock())
                    Tag.USER_HOME_LENGTH_PREFIX -> alias.targetInfo.userHomePrefixLen = stream.readShort()
                    null -> alias.extra.add(tagValue to stream.readBlob(length))
                }

                // Pad to 2-byte boundary
                if ((length and 1) != 0) {
                    stream.skip(1)
                }

                tagValue = stream.readShort()
            }

            return alias.build()
        }
    }

    class Builder(
        val appInfo: FourCC,
        val version: Short,
        val volumeInfo: VolumeInfo.Builder,
        val targetInfo: TargetInfo.Builder,
        val extra: MutableList<Pair<Short, Blob>> = mutableListOf()
    ) {
        fun build() = Alias(appInfo, version, volumeInfo.build(), targetInfo.build(), extra)
    }
}