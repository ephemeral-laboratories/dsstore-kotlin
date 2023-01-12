package garden.ephemeral.macfiles.alias

enum class VolumeType(val value: Short) {
    FIXED_DISK(0),
    NETWORK_DISK(1),
    FLOPPY_DISK_400KB(2),
    FLOPPY_DISK_800KB(3),
    FLOPPY_DISK_1_44MB(4),
    EJECTABLE_DISK(5),
    ;

    companion object {
        private val byValue by lazy {
            VolumeType.values().associateBy(VolumeType::value)
        }

        fun forValue(value: Short): VolumeType {
            return byValue[value] ?: throw IllegalArgumentException("Unrecognised value: $value")
        }
    }
}
