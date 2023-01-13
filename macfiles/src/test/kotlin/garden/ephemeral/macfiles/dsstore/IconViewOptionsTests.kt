package garden.ephemeral.macfiles.dsstore

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import garden.ephemeral.macfiles.alias.AliasReadTests
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.dsstore.types.DoublePoint
import garden.ephemeral.macfiles.dsstore.types.DoubleRgbColor
import garden.ephemeral.macfiles.dsstore.types.IconViewOptions
import garden.ephemeral.macfiles.dsstore.types.IntPoint
import org.junit.jupiter.api.Test

class IconViewOptionsTests {
    @Test
    fun `can read`() {
        DSStore.open(getFilePath("from-dmg.DS_Store")).use { store ->
            val options = store[".", DSStoreProperties.IconViewOptionsPList] as IconViewOptions

            assertThat(options.viewOptionsVersion).isEqualTo(1)

            assertThat(options.backgroundType).isEqualTo(2)
            assertThat(options.backgroundImageAlias).isEqualTo(AliasReadTests.FROM_DMG_ALIAS)
            assertThat(options.backgroundColor).isEqualTo(DoubleRgbColor.White)

            assertThat(options.gridOffset).isEqualTo(IntPoint(0, 0))
            assertThat(options.gridSpacing).isNotNull().isCloseTo(100.0, 0.0001)

            assertThat(options.arrangeBy).isEqualTo("none")

            assertThat(options.showIconPreview).isEqualTo(false)
            assertThat(options.showItemInfo).isEqualTo(false)
            assertThat(options.labelOnBottom).isEqualTo(true)

            assertThat(options.textSize).isNotNull().isCloseTo(11.0, 0.0001)
            assertThat(options.iconSize).isNotNull().isCloseTo(72.0, 0.0001)
            assertThat(options.scrollPosition).isEqualTo(DoublePoint(0.0, 0.0))
        }
    }

    @Test
    fun `can write`() {
        val options = IconViewOptions.build {
            viewOptionsVersion = 1

            backgroundType = 2
            backgroundImageAlias = AliasReadTests.FROM_DMG_ALIAS
            backgroundColor = DoubleRgbColor.White

            gridOffset = IntPoint(0, 0)
            gridSpacing = 100.0

            arrangeBy = "none"

            showIconPreview = false
            showItemInfo = false
            labelOnBottom = true

            textSize = 11.0
            iconSize = 72.0
            scrollPosition = DoublePoint(0.0, 0.0)
        }
        val blob = options.toBlob()
        // A lot of faith here that dd-plist won't suddenly change the order
        // they write the plist in, but if they change it, we can just retreat
        // back to reading the data back in and comparing the objects.
        assertThat(blob).isEqualTo(Blob.fromHexDump("""
            62 70 6c 69 73 74 30 30 df 10 11 01 02 03 04 05
            06 07 08 09 0a 0b 0c 0d 0e 0f 10 11 12 13 14 15
            15 15 16 16 17 18 19 19 1a 1b 1c 1d 1d 5f 10 12
            76 69 65 77 4f 70 74 69 6f 6e 73 56 65 72 73 69
            6f 6e 5e 62 61 63 6b 67 72 6f 75 6e 64 54 79 70
            65 5f 10 14 62 61 63 6b 67 72 6f 75 6e 64 49 6d
            61 67 65 41 6c 69 61 73 5f 10 12 62 61 63 6b 67
            72 6f 75 6e 64 43 6f 6c 6f 72 52 65 64 5f 10 14
            62 61 63 6b 67 72 6f 75 6e 64 43 6f 6c 6f 72 47
            72 65 65 6e 5f 10 13 62 61 63 6b 67 72 6f 75 6e
            64 43 6f 6c 6f 72 42 6c 75 65 5b 67 72 69 64 4f
            66 66 73 65 74 58 5b 67 72 69 64 4f 66 66 73 65
            74 59 5b 67 72 69 64 53 70 61 63 69 6e 67 59 61
            72 72 61 6e 67 65 42 79 5f 10 0f 73 68 6f 77 49
            63 6f 6e 50 72 65 76 69 65 77 5c 73 68 6f 77 49
            74 65 6d 49 6e 66 6f 5d 6c 61 62 65 6c 4f 6e 42
            6f 74 74 6f 6d 58 74 65 78 74 53 69 7a 65 58 69
            63 6f 6e 53 69 7a 65 5f 10 0f 73 63 72 6f 6c 6c
            50 6f 73 69 74 69 6f 6e 58 5f 10 0f 73 63 72 6f
            6c 6c 50 6f 73 69 74 69 6f 6e 59 10 01 10 02 4f
            11 01 50 00 00 00 00 01 50 00 02 00 00 04 41 63
            6d 65 00 00 00 00 00 00 00 00 00 00 00 00 00 00
            00 00 00 00 00 00 00 00 00 df c8 91 33 48 2b 00
            00 00 00 0a 34 0e 42 61 63 6b 67 72 6f 75 6e 64
            2e 70 6e 67 00 00 00 00 00 00 00 00 00 00 00 00
            00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
            00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
            00 00 00 00 00 00 00 0a 35 df c8 90 70 00 00 00
            00 00 00 00 00 ff ff ff ff 00 00 00 00 00 00 00
            00 00 00 00 00 00 00 00 00 00 00 00 0b 2e 62 61
            63 6b 67 72 6f 75 6e 64 00 00 10 00 08 00 00 df
            c8 91 33 00 00 00 11 00 08 00 00 df c8 90 70 00
            00 00 01 00 04 00 00 0a 34 00 02 00 20 41 63 6d
            65 3a 2e 62 61 63 6b 67 72 6f 75 6e 64 3a 00 42
            61 63 6b 67 72 6f 75 6e 64 2e 70 6e 67 00 0e 00
            1e 00 0e 00 42 00 61 00 63 00 6b 00 67 00 72 00
            6f 00 75 00 6e 00 64 00 2e 00 70 00 6e 00 67 00
            0f 00 0a 00 04 00 41 00 63 00 6d 00 65 00 12 00
            1b 2f 2e 62 61 63 6b 67 72 6f 75 6e 64 2f 42 61
            63 6b 67 72 6f 75 6e 64 2e 70 6e 67 00 00 13 00
            0d 2f 56 6f 6c 75 6d 65 73 2f 41 63 6d 65 00 ff
            ff 00 00 23 3f f0 00 00 00 00 00 00 10 00 23 40
            59 00 00 00 00 00 00 54 6e 6f 6e 65 08 09 23 40
            26 00 00 00 00 00 00 23 40 52 00 00 00 00 00 00
            23 00 00 00 00 00 00 00 00 00 08 00 2d 00 42 00
            51 00 68 00 7d 00 94 00 aa 00 b6 00 c2 00 ce 00
            d8 00 ea 00 f7 01 05 01 0e 01 17 01 29 01 3b 01
            3d 01 3f 02 93 02 9c 02 9e 02 a7 02 ac 02 ad 02
            ae 02 b7 02 c0 00 00 00 00 00 00 02 01 00 00 00
            00 00 00 00 1e 00 00 00 00 00 00 00 00 00 00 00
            00 00 00 02 c9
        """.trimIndent()))
    }
}