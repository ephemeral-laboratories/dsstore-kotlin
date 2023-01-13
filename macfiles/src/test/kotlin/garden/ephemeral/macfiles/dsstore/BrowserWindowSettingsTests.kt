package garden.ephemeral.macfiles.dsstore

import assertk.assertThat
import assertk.assertions.isEqualTo
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.dsstore.types.BrowserWindowSettings
import org.junit.jupiter.api.Test

class BrowserWindowSettingsTests {
    @Test
    fun `can read`() {
        DSStore.open(getFilePath("from-dmg.DS_Store")).use { store ->
            val options = store[".", DSStoreProperties.BrowserWindowSettings] as BrowserWindowSettings

            assertThat(options.containerShowSidebar).isEqualTo(false)
            assertThat(options.previewPaneVisibility).isEqualTo(false)
            assertThat(options.showPathbar).isEqualTo(false)
            assertThat(options.showSidebar).isEqualTo(false)
            assertThat(options.showStatusBar).isEqualTo(false)
            assertThat(options.showTabView).isEqualTo(false)
            assertThat(options.showToolbar).isEqualTo(false)
            assertThat(options.sidebarWidth).isEqualTo(0)
            assertThat(options.windowBounds).isEqualTo("{{400, 100}, {512, 343}}")
        }
    }

    @Test
    fun `can write`() {
        val options = BrowserWindowSettings.build {
            containerShowSidebar = true
            previewPaneVisibility = true
            showPathbar = true
            showSidebar = true
            showStatusBar = true
            showTabView = true
            showToolbar = true
            sidebarWidth = 42
            windowBounds = "{{400, 100}, {512, 343}}"
        }
        val blob = options.toBlob()
        assertThat(blob).isEqualTo(
            Blob.fromHexDump("""
                62 70 6c 69 73 74 30 30 d9 01 02 03 04 05 06 07
                08 09 0a 0a 0a 0a 0a 0a 0a 0b 0c 5f 10 14 63 6f
                6e 74 61 69 6e 65 72 53 68 6f 77 53 69 64 65 62
                61 72 5f 10 15 70 72 65 76 69 65 77 50 61 6e 65
                56 69 73 69 62 69 6c 69 74 79 5b 73 68 6f 77 50
                61 74 68 62 61 72 5b 73 68 6f 77 53 69 64 65 62
                61 72 5d 73 68 6f 77 53 74 61 74 75 73 42 61 72
                5b 73 68 6f 77 54 61 62 56 69 65 77 5b 73 68 6f
                77 54 6f 6f 6c 62 61 72 5c 73 69 64 65 62 61 72
                57 69 64 74 68 5c 77 69 6e 64 6f 77 42 6f 75 6e
                64 73 09 10 2a 5f 10 18 7b 7b 34 30 30 2c 20 31
                30 30 7d 2c 20 7b 35 31 32 2c 20 33 34 33 7d 7d
                08 1b 32 4a 56 62 70 7c 88 95 a2 a3 a5 00 00 00
                00 00 00 01 01 00 00 00 00 00 00 00 0d 00 00 00
                00 00 00 00 00 00 00 00 00 00 00 00 c0
            """.trimIndent()))
    }
}
