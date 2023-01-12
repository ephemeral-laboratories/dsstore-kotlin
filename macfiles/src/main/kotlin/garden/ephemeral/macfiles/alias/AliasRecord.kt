package garden.ephemeral.macfiles.alias

import garden.ephemeral.macfiles.common.io.DataOutput

interface AliasRecord {
    /**
     * Creates a builder to start building the volume information,
     * and populates it with information from this record.
     *
     * @return the builder.
     */
    fun deriveVolumeInfo(): VolumeInfo.Builder

    /**
     * Creates a builder to start building the target information,
     * and populates it with information from this record.
     *
     * @return the builder.
     */
    fun deriveTargetInfo(): TargetInfo.Builder

    /**
     * Writes the alias record to the provided stream.
     *
     * @param stream the stream to write to.
     */
    fun writeTo(stream: DataOutput)

}
