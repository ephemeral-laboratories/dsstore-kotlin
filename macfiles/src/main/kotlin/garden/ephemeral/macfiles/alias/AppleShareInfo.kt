package garden.ephemeral.macfiles.alias

/**
 * Holds information about where the file was when accessed via AppleShare.
 *
 * @property zone the AppleShare zone.
 * @property server the AFP server.
 * @property user the username.
 */
data class AppleShareInfo(
    val zone: String?,
    val server: String?,
    val user: String?
) {
    class Builder(
        var zone: String? = null,
        var server: String? = null,
        var user: String? = null
    ) {
        fun build() = AppleShareInfo(zone, server, user)
    }
}
