package garden.ephemeral.macfiles.common

import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

object MacTimeUtils {
    private val MacClassicEpoch = ZonedDateTime.of(1904, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
    private val MacOSEpoch = ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()

    private const val MacDateDivisor = 65536

    fun decodeHighResInstant(value: Long): Instant {
        return MacClassicEpoch
            .plusSeconds(value / MacDateDivisor)
            .plusNanos(1_000_000_000L * (value % MacDateDivisor) / MacDateDivisor)
    }

    fun encodeHighResInstant(value: Instant): Long {
        val duration = Duration.between(MacClassicEpoch, value)
        return duration.seconds * MacDateDivisor + (duration.nano * MacDateDivisor / 1_000_000_000L)
    }

    fun decodeDoubleInstant(value: Double): Instant {
        return MacOSEpoch
            .plusSeconds(value.toLong())
            .plusNanos((value.mod(1.0) * 1_000_000_000).toLong())
    }

    fun encodeDoubleInstant(value: Instant): Double {
        val duration = Duration.between(MacOSEpoch, value)
        return duration.seconds + duration.nano / 1_000_000_000.0
    }

    fun decodeLowResInstant(value: UInt): Instant {
        return MacClassicEpoch.plusSeconds(value.toLong())
    }

    fun encodeLowResInstant(value: Instant): UInt {
        return Duration.between(MacClassicEpoch, value).toSeconds().toUInt()
    }
}
