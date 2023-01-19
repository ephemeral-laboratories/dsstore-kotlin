package garden.ephemeral.macfiles.dsstore

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import garden.ephemeral.macfiles.alias.AliasReadTests
import garden.ephemeral.macfiles.bookmark.Bookmark
import garden.ephemeral.macfiles.bookmark.BookmarkKeys
import garden.ephemeral.macfiles.bookmark.types.URL
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.common.types.FourCC
import garden.ephemeral.macfiles.dsstore.types.*
import java.time.Instant
import kotlin.test.Test

class DSStoreReadTests {
    @Test
    fun testCanWalkTrivialFile() {
        DSStore.open(getFilePath("trivial.DS_Store")).use { store ->
            val records = store.walk().toList()
            assertThat(records[0].filename).isEqualTo("bam")
            assertThat(records[1].filename).isEqualTo("bar")
            assertThat(records[2].filename).isEqualTo("baz")
        }
    }

    @Test
    fun testCanFindRecordsInTrivialFile() {
        DSStore.open(getFilePath("trivial.DS_Store")).use { store ->
            assertThat(store["bam", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(104, 116))
            assertThat(store["bar", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(256, 235))
            assertThat(store["baz", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(454, 124))
        }
    }

    @Test
    fun testCanWalkLargerFile() {
        DSStore.open(getFilePath("large.DS_Store")).use { store ->
            val recordCount = store.walk().count()
            assertThat(recordCount).isEqualTo(10128)
        }
    }

    @Test
    fun testCanFindRecordsInLargerFile() {
        DSStore.open(getFilePath("large.DS_Store")).use { store ->
            // Examples cunningly selected to be near boundaries to get maximum code coverage
            assertThat(store["2188q7ay6JNcqKwXy1uJH29GD6DrpTNbVkJDKFNSR1ma", DSStoreProperties.PhysicalSize1])
                .isEqualTo(8192L)
            assertThat(store["ZMtXWkRQ9DGXoYPr7BFm4dRQzmmj5kRS1CPeDMPYnWd", DSStoreProperties.PhysicalSize1])
                .isEqualTo(8192L)
            // Not found cases to complete the coverage
            assertThat(store["7RXeGbHV1LMHnSWb9CgbRkSL789mGrTJb6gUULQyz2wD", DSStoreProperties.PhysicalSize])
                .isNull()
            assertThat(store["ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ", DSStoreProperties.PhysicalSize1])
                .isNull()
        }
    }

    @Test
    fun testExampleFromDmg() {
        DSStore.open(getFilePath("from-dmg.DS_Store")).use { store ->
            assertThat(store[".", DSStoreProperties.ViewStyle2]).isEqualTo(FourCC("icnv"))
            assertThat(store[".", DSStoreProperties.UnknownDirectoryTag]).isEqualTo(1)
            assertThat(store[".background", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(120, 350))
            assertThat(store[".fseventsd", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(393, 350))
            assertThat(store["Applications", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(393, 180))
            assertThat(store["Noot.app", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(120, 180))

            assertThat(store[".", DSStoreProperties.BrowserWindowSettings]).isEqualTo(
                BrowserWindowSettings.build {
                    containerShowSidebar = false
                    previewPaneVisibility = false
                    showPathbar = false
                    showSidebar = false
                    showStatusBar = false
                    showTabView = false
                    showToolbar = false
                    sidebarWidth = 0
                    windowBounds = "{{400, 100}, {512, 343}}"
                }
            )

            assertThat(store[".", DSStoreProperties.IconViewOptionsPList]).isEqualTo(
                IconViewOptions.build {
                    viewOptionsVersion = 1
                    arrangeBy = "none"
                    backgroundColor = DoubleRgbColor.White
                    backgroundImageAlias = AliasReadTests.FROM_DMG_ALIAS
                    backgroundType = 2
                    gridOffset = IntPoint(0, 0)
                    gridSpacing = 100.0
                    iconSize = 72.0
                    labelOnBottom = true
                    scrollPosition = DoublePoint(0.0, 0.0)
                    showIconPreview = false
                    showItemInfo = false
                    textSize = 11.0
                }
            )

            assertThat(store[".", DSStoreProperties.BackgroundBookmark]).isEqualTo(
                Bookmark.build {
                    put(BookmarkKeys.kBookmarkPath, listOf(".background", "Background.png"))
                    put(BookmarkKeys.kBookmarkCNIDPath, listOf(2612, 2613))
                    put(
                        BookmarkKeys.kBookmarkFileProperties,
                        Blob.fromHexDump(
                            """
                            01 00 00 00 00 00 00 00 0f 00 00 00 00 00 00 00
                            00 00 00 00 00 00 00 00
                            """.trimIndent()
                        )
                    )
                    put(BookmarkKeys.kBookmarkFileCreationDate, Instant.parse("2022-12-21T10:29:04Z"))
                    put(BookmarkKeys.kBookmarkVolumePath, "/Volumes/Noot")
                    put(BookmarkKeys.kBookmarkVolumeURL, URL.Absolute("file:///Volumes/Noot"))
                    put(BookmarkKeys.kBookmarkVolumeName, "Noot")
                    put(BookmarkKeys.kBookmarkVolumeUUID, "3AB829F0-09FF-3AC0-9987-0875AD435322")
                    put(BookmarkKeys.kBookmarkVolumeSize, 2148413440)
                    put(BookmarkKeys.kBookmarkVolumeCreationDate, Instant.parse("2022-12-21T10:32:19Z"))
                    put(
                        BookmarkKeys.kBookmarkVolumeProperties,
                        Blob.fromHexDump(
                            """
                            81 00 00 00 01 00 00 00 ef 13 00 00 01 00 00 00
                            00 00 00 00 00 00 00 00 
                            """.trimIndent()
                        )
                    )
                    put(BookmarkKeys.kBookmarkVolumeIsRoot, false)
                    put(BookmarkKeys.kBookmarkContainingFolder, 0)
                    put(BookmarkKeys.kBookmarkUserName, "unknown")
                    put(BookmarkKeys.kBookmarkUID, 99)
                    put(BookmarkKeys.kBookmarkWasFileReference, true)
                    put(BookmarkKeys.kBookmarkCreationOptions, 512)
                }
            )
        }
    }
}
