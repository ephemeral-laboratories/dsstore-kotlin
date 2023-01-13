package garden.ephemeral.macfiles.dsstore.codecs

import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.dsstore.types.IconViewOptions

object IconViewOptionsCodec : Codec<IconViewOptions> {
    override fun decode(blob: Blob) = IconViewOptions.fromBlob(blob)
    override fun encode(value: IconViewOptions) = value.toBlob()
}