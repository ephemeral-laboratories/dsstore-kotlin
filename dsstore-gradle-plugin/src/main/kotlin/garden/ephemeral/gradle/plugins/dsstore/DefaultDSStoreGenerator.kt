package garden.ephemeral.gradle.plugins.dsstore

import garden.ephemeral.macfiles.common.types.FourCC
import garden.ephemeral.macfiles.dsstore.DSStore
import garden.ephemeral.macfiles.dsstore.DSStoreProperties
import garden.ephemeral.macfiles.dsstore.types.*
import garden.ephemeral.macfiles.dsstore.util.FileMode
import garden.ephemeral.macfiles.native.aliasForFile
import garden.ephemeral.macfiles.native.bookmarkForFile
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import java.io.File
import javax.inject.Inject

/**
 * Encapsulation of the logic for converting DSL config into actual `.DS_Store` entries.
 *
 * Can be used directly instead of the task, in the event that you want to use
 * the same DSL to configure the file generation, but don't want to be constrained
 * to doing so with a Gradle task.
 */
abstract class DefaultDSStoreGenerator @Inject constructor(private val project: Project) : DSStoreGenerator {
    @get:OutputFile
    abstract override val outputFile: RegularFileProperty

    @get:Nested
    val rootItemConfig: RootItemConfig = project.objects.newInstance(RootItemConfig::class.java)

    @get:Nested
    val itemConfigByName = mutableMapOf<String, ItemConfig>()

    override fun root(configBlock: RootItemConfig.() -> Unit) {
        configBlock(rootItemConfig)
    }

    override fun item(name: String, configBlock: ItemConfig.() -> Unit) {
        val itemConfig = itemConfigByName.getOrPut(name) {
            project.objects.newInstance(ItemConfig::class.java)
        }
        configBlock(itemConfig)
    }

    override fun generate() {
        DSStore.open(outputFile.get().asFile.toPath(), FileMode.READ_WRITE).use { store ->
            // Compulsory stuff for the directory itself.
            // TODO: Original has this as `('long', 1)` - check that just 1L works
            store[".", DSStoreProperties.UnknownDirectoryTag] = 1L
            store[".", DSStoreProperties.ViewStyle2] = FourCC("icnv")

            val backgroundImagePath = rootItemConfig.backgroundImage.asFile.orNull
            if (backgroundImagePath != null) {
                store[".", DSStoreProperties.BackgroundBookmark] = bookmarkForFile(backgroundImagePath)
            }
            store[".", DSStoreProperties.IconViewOptionsPList] = makeIconViewOptions(backgroundImagePath)
            store[".", DSStoreProperties.BrowserWindowSettings] = makeBrowserWindowSettings(rootItemConfig)
            itemConfigByName.forEach { (name, config) ->
                config.iconLocation.orNull?.let { location ->
                    store[name, DSStoreProperties.IconLocation] = IntPoint(location.x, location.y)
                }
            }
        }
    }

    /**
     * Makes [IconViewOptions] for the store.
     *
     * @return the icon view options.
     */
    private fun makeIconViewOptions(backgroundImagePath: File?) = IconViewOptions.build {
        viewOptionsVersion = 1
        gridOffset = IntPoint(0, 0)
        gridSpacing = 100.0
        arrangeBy = "none"
        showIconPreview = false
        showItemInfo = false
        labelOnBottom = true
        textSize = 11.0
        iconSize = 72.0
        scrollPosition = DoublePoint(0.0, 0.0)

        backgroundColor = DoubleRgbColor.White
        if (backgroundImagePath != null) {
            backgroundType = 2 // image
            backgroundImageAlias = aliasForFile(backgroundImagePath)
        } else {
            backgroundType = 1 // color
        }
    }

    /**
     * Makes [BrowserWindowSettings] for the store.
     *
     * @return the browser window settings.
     */
    private fun makeBrowserWindowSettings(rootItemConfig: RootItemConfig) = BrowserWindowSettings.build {
        val windowSize = rootItemConfig.windowSize.getOrElse(RootItemConfig.WindowSize(400, 300))

        showStatusBar = false
        windowBounds = "{{400, 100}, {${windowSize.width}, ${windowSize.height}}}"
        containerShowSidebar = false
        previewPaneVisibility = false
        sidebarWidth = 0
        showTabView = false
        showToolbar = false
        showPathbar = false
        showSidebar = false
    }
}
