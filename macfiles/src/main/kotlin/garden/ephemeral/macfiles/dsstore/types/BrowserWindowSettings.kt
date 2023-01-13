package garden.ephemeral.macfiles.dsstore.types

import com.dd.plist.BinaryPropertyListParser
import com.dd.plist.BinaryPropertyListWriter
import com.dd.plist.NSDictionary
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.dsstore.util.DictionaryDelegates

data class BrowserWindowSettings(private val dictionary: NSDictionary) {
    val containerShowSidebar by DictionaryDelegates.booleanFrom(dictionary, "ContainerShowSidebar")
    val previewPaneVisibility by DictionaryDelegates.booleanFrom(dictionary, "PreviewPaneVisibility")
    val showPathbar by DictionaryDelegates.booleanFrom(dictionary, "ShowPathbar")
    val showSidebar by DictionaryDelegates.booleanFrom(dictionary, "ShowSidebar")
    val showStatusBar by DictionaryDelegates.booleanFrom(dictionary, "ShowStatusBar")
    val showTabView by DictionaryDelegates.booleanFrom(dictionary, "ShowTabView")
    val showToolbar by DictionaryDelegates.booleanFrom(dictionary, "ShowToolbar")
    val sidebarWidth by DictionaryDelegates.intFrom(dictionary, "SidebarWidth")
    val windowBounds by DictionaryDelegates.stringFrom(dictionary, "WindowBounds")

    fun toBlob(): Blob {
        val data = BinaryPropertyListWriter.writeToArray(dictionary)
        return Blob(data)
    }

    companion object {
        fun fromBlob(blob: Blob): BrowserWindowSettings {
            val plist = BinaryPropertyListParser.parse(blob.toByteArray())
            return BrowserWindowSettings(plist as NSDictionary)
        }

        fun build(action: Builder.() -> Unit): BrowserWindowSettings {
            return Builder().apply(action).build()
        }
    }

    class Builder {
        private val dictionary: NSDictionary = NSDictionary()

        var containerShowSidebar by DictionaryDelegates.booleanFrom(dictionary, "ContainerShowSidebar")
        var previewPaneVisibility by DictionaryDelegates.booleanFrom(dictionary, "PreviewPaneVisibility")
        var showPathbar by DictionaryDelegates.booleanFrom(dictionary, "ShowPathbar")
        var showSidebar by DictionaryDelegates.booleanFrom(dictionary, "ShowSidebar")
        var showStatusBar by DictionaryDelegates.booleanFrom(dictionary, "ShowStatusBar")
        var showTabView by DictionaryDelegates.booleanFrom(dictionary, "ShowTabView")
        var showToolbar by DictionaryDelegates.booleanFrom(dictionary, "ShowToolbar")
        var sidebarWidth by DictionaryDelegates.intFrom(dictionary, "SidebarWidth")
        var windowBounds by DictionaryDelegates.stringFrom(dictionary, "WindowBounds")

        init {
            containerShowSidebar = false
            previewPaneVisibility = false
            showPathbar = false
            showSidebar = false
            showStatusBar = false
            showTabView = false
            showToolbar = false
            sidebarWidth = 0
        }

        fun build(): BrowserWindowSettings {
            return BrowserWindowSettings(dictionary.clone())
        }
    }
}