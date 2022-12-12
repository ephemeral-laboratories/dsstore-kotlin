package garden.ephemeral.macfiles.alias

enum class Tag(var value: Int) {
    CARBON_FOLDER_NAME(0),
    CNID_PATH(1),
    CARBON_PATH(2),
    APPLESHARE_ZONE(3),
    APPLESHARE_SERVER_NAME(4),
    APPLESHARE_USERNAME(5),
    DRIVER_NAME(6),
    NETWORK_MOUNT_INFO(9),
    DIALUP_INFO(10),
    UNICODE_FILENAME(14),
    UNICODE_VOLUME_NAME(15),
    HIGH_RES_VOLUME_CREATION_DATE(16),
    HIGH_RES_CREATION_DATE(17),
    POSIX_PATH(18),
    POSIX_PATH_TO_MOUNTPOINT(19),
    RECURSIVE_ALIAS_OF_DISK_IMAGE(20),
    USER_HOME_LENGTH_PREFIX(21),
    ;

    companion object {
        private val byValue by lazy {
            Tag.values().associateBy(Tag::value)
        }

        fun findForValue(value: Short) = byValue[value.toInt()]
    }
}
