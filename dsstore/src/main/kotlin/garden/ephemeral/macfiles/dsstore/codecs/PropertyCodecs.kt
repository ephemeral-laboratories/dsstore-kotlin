package garden.ephemeral.macfiles.dsstore.codecs

import garden.ephemeral.macfiles.dsstore.DSStoreProperties
import garden.ephemeral.macfiles.dsstore.types.FourCC

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
        fun findCodec(propertyId: FourCC): Codec<Any>? {
            // Hard-to-avoid situation because we want to be able to use arbitrary types for in _and_ out.
            @Suppress("UNCHECKED_CAST")
            return codecMap[propertyId] as Codec<Any>?
        }
    }
}