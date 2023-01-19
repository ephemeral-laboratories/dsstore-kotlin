package garden.ephemeral.macfiles.dsstore

import assertk.assertThat
import assertk.assertions.isEqualTo
import garden.ephemeral.macfiles.dsstore.types.BrowserWindowSettings
import org.junit.jupiter.api.Test

class BrowserWindowSettingsTests {
    @Test
    fun `can read`() {
        DSStore.open(getFilePath("from-dmg.DS_Store")).use { store ->
            val settings = store[".", DSStoreProperties.BrowserWindowSettings] as BrowserWindowSettings

            assertThat(settings.containerShowSidebar).isEqualTo(false)
            assertThat(settings.previewPaneVisibility).isEqualTo(false)
            assertThat(settings.showPathbar).isEqualTo(false)
            assertThat(settings.showSidebar).isEqualTo(false)
            assertThat(settings.showStatusBar).isEqualTo(false)
            assertThat(settings.showTabView).isEqualTo(false)
            assertThat(settings.showToolbar).isEqualTo(false)
            assertThat(settings.sidebarWidth).isEqualTo(0)
            assertThat(settings.windowBounds).isEqualTo("{{400, 100}, {512, 343}}")
        }
    }

    @Test
    fun `can write`() {
        val settings = BrowserWindowSettings.build {
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
        val blob = settings.toBlob()
        // The dictionary shuffles data so we can't use exact byte matching
        assertThat(BrowserWindowSettings.fromBlob(blob)).isEqualTo(settings)
    }
}
