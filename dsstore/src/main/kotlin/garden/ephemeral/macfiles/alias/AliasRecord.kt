package garden.ephemeral.macfiles.alias

interface AliasRecord {
    fun deriveVolumeInfo(): VolumeInfo.Builder
    fun deriveTargetInfo(): TargetInfo.Builder
}
