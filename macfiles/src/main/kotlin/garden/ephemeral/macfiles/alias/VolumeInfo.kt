package garden.ephemeral.macfiles.alias

import garden.ephemeral.macfiles.common.types.Blob
import java.time.Instant

/**
 * Holds information about the volume on which the target resides.
 *
 * @property name the name of the volume.
 * @property creationDate the creation date of the target's volume.
 * @property fsType the filesystem type.
 *           For v2 aliases, this is a 2-character code;
 *           for v3 aliases, a 4-character code.
 * @property diskType the type of disk.
 * @property attributeFlags the filesystem attribute flags (from HFS volume header).
 * @property fsId the filesystem identifier.
 * @property appleShareInfo AppleShare information (for automatic remounting of network shares).
 * @property driverName the driver name (*probably* contains a disk driver name on older Macs).
 * @property posixPath the POSIX path of the mount point of the target's volume.
 * @property diskImageAlias a nested alias pointing at the disk image on which the
 *           target's volume resides.
 * @property dialupInfo dialup information (for automatic establishment of dialup connections).
 * @property networkMountInfo network mount information (for automatic remounting).
 */
data class VolumeInfo(
    val name: String,
    val creationDate: Instant,
    val fsType: String,
    val diskType: VolumeType,
    val attributeFlags: UInt,
    val fsId: String?,
    val appleShareInfo: AppleShareInfo? = null,
    val driverName: String? = null,
    val posixPath: String? = null,
    val diskImageAlias: Alias? = null,
    val dialupInfo: Blob? = null,
    val networkMountInfo: Blob? = null,

    /*
    @property
    def filesystem_type(self):
        return ALIAS_FSTYPE_MAP.get(self.fs_type, ALIAS_FILESYSTEM_UNKNOWN)

    def __repr__(self):
        args = [
            "name",
            "creation_date",
            "fs_type",
            "disk_type",
            "attribute_flags",
            "fs_id",
        ]
        values = []
        for a in args:
            v = getattr(self, a)
            values.append(repr(v))

        kwargs = [
            "appleshare_info",
            "driver_name",
            "posix_path",
            "disk_image_alias",
            "dialup_info",
            "network_mount_info",
        ]
        for a in kwargs:
            v = getattr(self, a)
            if v is not None:
                values.append(f"{a}={v!r}")
        return "VolumeInfo(%s)" % ",".join(values)

     */
) {
    class Builder(
        var name: String,
        var creationDate: Instant,
        val fsType: String,
        val diskType: VolumeType,
        val attributeFlags: UInt,
        val fsId: String? = null,
        var appleShareInfo: AppleShareInfo.Builder? = null,
        var driverName: String? = null,
        var posixPath: String? = null,
        var diskImageAlias: Alias? = null,
        var dialupInfo: Blob? = null,
        var networkMountInfo: Blob? = null,
    ) {
        fun lazyAppleShareInfo(): AppleShareInfo.Builder {
            var appleShareInfo = this.appleShareInfo
            if (appleShareInfo == null) {
                appleShareInfo = AppleShareInfo.Builder()
                this.appleShareInfo = appleShareInfo
            }
            return appleShareInfo
        }

        fun build() = VolumeInfo(
            name, creationDate, fsType, diskType, attributeFlags, fsId,
            appleShareInfo?.build(),
            driverName, posixPath, diskImageAlias, dialupInfo, networkMountInfo
        )
    }

}
