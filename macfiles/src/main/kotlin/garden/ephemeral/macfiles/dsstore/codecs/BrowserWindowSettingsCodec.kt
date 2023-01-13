package garden.ephemeral.macfiles.dsstore.codecs

import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.dsstore.types.BrowserWindowSettings

object BrowserWindowSettingsCodec : Codec<BrowserWindowSettings> {
    override fun decode(blob: Blob) = BrowserWindowSettings.fromBlob(blob)
    override fun encode(value: BrowserWindowSettings) = value.toBlob()
}