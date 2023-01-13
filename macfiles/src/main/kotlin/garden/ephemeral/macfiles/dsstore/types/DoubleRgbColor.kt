package garden.ephemeral.macfiles.dsstore.types

/**
 * An RGB color with double values 0.0~1.0.
 *
 * @property r the red value.
 * @property g the green value.
 * @property b the blue value.
 */
data class DoubleRgbColor(
    val r: Double,
    val g: Double,
    val b: Double
) {
    companion object {
        val Black = DoubleRgbColor(0.0, 0.0, 0.0)
        val White = DoubleRgbColor(1.0, 1.0, 1.0)
    }
}
