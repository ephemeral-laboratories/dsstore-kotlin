package codecs

import DSStoreProperties
import types.FourCC

/**
 * Repository of codecs for known properties.
 */
class PropertyCodecs {
    companion object {
        private val codecMap = mapOf(
            DSStoreProperties.IconLocation to IconLocationCodec,
        )

        /**
         * Finds a property codec.
         *
         * @param propertyId the property ID to look up. See [DSStoreProperties] for values.
         * @return the codec, if found, or `null` if none was found.
         */
        fun findCodec(propertyId: FourCC): Codec<out Any>? {
            return codecMap[propertyId]
        }
    }
}