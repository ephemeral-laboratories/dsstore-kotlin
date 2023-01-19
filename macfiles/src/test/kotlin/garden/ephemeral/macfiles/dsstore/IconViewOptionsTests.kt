package garden.ephemeral.macfiles.dsstore

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import garden.ephemeral.macfiles.alias.AliasReadTests
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
        // The dictionary shuffles data so we can't use exact byte matching
        assertThat(IconViewOptions.fromBlob(blob)).isEqualTo(options)
    }
}