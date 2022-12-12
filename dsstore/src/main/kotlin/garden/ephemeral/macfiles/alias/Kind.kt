package garden.ephemeral.macfiles.alias

enum class Kind(var value: Int) {
    FILE(0),
    FOLDER(1),
    ;

    companion object {
        private val byValue by lazy {
            Kind.values().associateBy(Kind::value)
        }

        fun forValue(value: Short): Kind {
            return byValue[value.toInt()] ?: throw IllegalArgumentException("Unrecognised value: $value")
        }
    }
}
