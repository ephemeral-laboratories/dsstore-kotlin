package garden.ephemeral.macfiles.bookmark

import assertk.assertThat
import assertk.assertions.isEqualTo
import garden.ephemeral.macfiles.bookmark.types.URL
import garden.ephemeral.macfiles.bookmark.types.UUID
import garden.ephemeral.macfiles.common.types.Blob
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class BookmarkWriteTests {

    @Test
    fun `reproducing existing example`() {
        val bookmark = BookmarkReadTests.EXPECTED_FILE_BOOKMARK

        val blob = bookmark.toBlob()

        // We can't be sure that the padding and so forth are identical to what macOS produced,
        // so the best we have is reading it back in and comparing against what we had.
        assertThat(Bookmark.readFrom(blob)).isEqualTo(BookmarkReadTests.EXPECTED_FILE_BOOKMARK)
    }

    @Test
    fun `string keys`() {
        val bookmark = Bookmark.build {
            // Example I found somewhere
            // 8a529060-04b1-11e9-9b52-31bbfe39da0e === <11 e9 04 b1 8a 52 90 60 9b 52 31 bb fe 39 da 0e>
            put(TocKey.OfString("Custom"), 42)
        }
        val blob = bookmark.toBlob()
        assertThat(Bookmark.readFrom(blob)).isEqualTo(bookmark)
    }

    @ParameterizedTest
    @MethodSource("exoticTypeExamples")
    fun `exotic types`(example: Any?) {
        // Tries to test all the features real bookmark files I had didn't seem to be using
        val bookmark = Bookmark.build {
            put(TocKey.OfString("CustomUUID"), example)
        }

        val blob = bookmark.toBlob()
        assertThat(Bookmark.readFrom(blob)).isEqualTo(bookmark)
    }

    @Test
    fun `extra tables of contents`() {
        val bookmark = Bookmark.build {
            put(BookmarkKeys.kBookmarkDisplayName, "Alice")

            extraToc(42) {
                put(BookmarkKeys.kBookmarkDisplayName, "Bob")
            }
        }

        val blob = bookmark.toBlob()
        assertThat(Bookmark.readFrom(blob)).isEqualTo(bookmark)
    }

    companion object {
        @JvmStatic
        fun exoticTypeExamples() = listOf(
            null,
            42.toByte(),
            135.toShort(),
            246.0f,
            357.0,

            // Example UUID I found somewhere
            // 8a529060-04b1-11e9-9b52-31bbfe39da0e === <11 e9 04 b1 8a 52 90 60 9b 52 31 bb fe 39 da 0e>
            UUID(
                Blob(
                    byteArrayOf(
                        0x11, 0xe9.toByte(), 0x04, 0xb1.toByte(),
                        0x8a.toByte(), 0x52, 0x90.toByte(), 0x60,
                        0x9b.toByte(), 0x52, 0x31, 0xbb.toByte(),
                        0xfe.toByte(), 0x39, 0xda.toByte(), 0x0e,
                    )
                )
            ),

            URL.Relative(
                URL.Relative(
                    URL.Absolute("https://www.reddit.com"),
                    "r/"
                ), "ProgrammerHumor/"
            ),

            mapOf(
                1 to "fish",
                2 to "fish",
                "red" to "fish",
                "blue" to "fish"
            )
        )
    }
}