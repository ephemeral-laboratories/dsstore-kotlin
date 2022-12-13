package garden.ephemeral.macfiles.alias

enum class Kind(var value: Short) {
    FILE(0),
    FOLDER(1),
    ;

    companion object {
        private val byValue by lazy {
            Kind.values().associateBy(Kind::value)
        }

        fun forValue(value: Short): Kind {
            return byValue[value] ?: throw IllegalArgumentException("Unrecognised value: $value")
        }
    }
}
