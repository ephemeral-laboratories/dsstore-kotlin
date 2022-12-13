package garden.ephemeral.macfiles.bookmark.types

import garden.ephemeral.macfiles.common.types.Blob

data class Unrecognised(
    val typeCode: Int,
    val data: Blob
)