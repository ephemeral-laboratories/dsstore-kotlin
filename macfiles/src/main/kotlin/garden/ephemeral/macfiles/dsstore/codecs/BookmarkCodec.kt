package garden.ephemeral.macfiles.dsstore.codecs

import garden.ephemeral.macfiles.bookmark.Bookmark
import garden.ephemeral.macfiles.common.types.Blob

object BookmarkCodec : Codec<Bookmark> {
    override fun decode(blob: Blob): Bookmark {
        return Bookmark.readFrom(blob)
    }

    override fun encode(value: Bookmark): Blob {
        return value.toBlob()
    }
}