package garden.ephemeral.macfiles.dsstore.types

import com.dd.plist.BinaryPropertyListParser
import com.dd.plist.BinaryPropertyListWriter
import com.dd.plist.NSDictionary
import garden.ephemeral.macfiles.common.types.Blob
import garden.ephemeral.macfiles.dsstore.util.CompositeDelegates
import garden.ephemeral.macfiles.dsstore.util.DictionaryDelegates

/**
 * Options for Icon view.
 */
data class IconViewOptions(private val dictionary: NSDictionary) {
    val viewOptionsVersion by DictionaryDelegates.intFrom(dictionary)

    val backgroundType by DictionaryDelegates.intFrom(dictionary)
    val backgroundImageAlias by DictionaryDelegates.aliasFrom(dictionary)
    private val backgroundColorRed by DictionaryDelegates.doubleFrom(dictionary)
    private val backgroundColorGreen by DictionaryDelegates.doubleFrom(dictionary)
    private val backgroundColorBlue by DictionaryDelegates.doubleFrom(dictionary)
    val backgroundColor by CompositeDelegates.doubleRgbColor(::backgroundColorRed, ::backgroundColorGreen, ::backgroundColorBlue)

    private val gridOffsetX by DictionaryDelegates.intFrom(dictionary)
    private val gridOffsetY by DictionaryDelegates.intFrom(dictionary)
    val gridOffset by CompositeDelegates.intPoint(::gridOffsetX, ::gridOffsetY)
    val gridSpacing by DictionaryDelegates.doubleFrom(dictionary)

    val arrangeBy by DictionaryDelegates.stringFrom(dictionary)

    val showIconPreview by DictionaryDelegates.booleanFrom(dictionary)
    val showItemInfo by DictionaryDelegates.booleanFrom(dictionary)
    val labelOnBottom by DictionaryDelegates.booleanFrom(dictionary)

    val textSize by DictionaryDelegates.doubleFrom(dictionary)
    val iconSize by DictionaryDelegates.doubleFrom(dictionary)
    private val scrollPositionX by DictionaryDelegates.doubleFrom(dictionary)
    private val scrollPositionY by DictionaryDelegates.doubleFrom(dictionary)
    val scrollPosition by CompositeDelegates.doublePoint(::scrollPositionX, ::scrollPositionY)

    fun toBlob(): Blob {
        val data = BinaryPropertyListWriter.writeToArray(dictionary)
        return Blob(data)
    }

    companion object {
        fun fromBlob(blob: Blob): IconViewOptions {
            val plist = BinaryPropertyListParser.parse(blob.toByteArray())
            return IconViewOptions(plist as NSDictionary)
        }

        fun build(action: Builder.() -> Unit): IconViewOptions {
            return Builder().apply(action).build()
        }
    }

    class Builder {
        private val dictionary: NSDictionary = NSDictionary()

        var viewOptionsVersion by DictionaryDelegates.intFrom(dictionary)

        var backgroundType by DictionaryDelegates.intFrom(dictionary)
        var backgroundImageAlias by DictionaryDelegates.aliasFrom(dictionary)
        private var backgroundColorRed by DictionaryDelegates.doubleFrom(dictionary)
        private var backgroundColorGreen by DictionaryDelegates.doubleFrom(dictionary)
        private var backgroundColorBlue by DictionaryDelegates.doubleFrom(dictionary)
        var backgroundColor by CompositeDelegates.doubleRgbColor(::backgroundColorRed, ::backgroundColorGreen, ::backgroundColorBlue)

        private var gridOffsetX by DictionaryDelegates.intFrom(dictionary)
        private var gridOffsetY by DictionaryDelegates.intFrom(dictionary)
        var gridOffset by CompositeDelegates.intPoint(::gridOffsetX, ::gridOffsetY)
        var gridSpacing by DictionaryDelegates.doubleFrom(dictionary)

        var arrangeBy by DictionaryDelegates.stringFrom(dictionary)

        var showIconPreview by DictionaryDelegates.booleanFrom(dictionary)
        var showItemInfo by DictionaryDelegates.booleanFrom(dictionary)
        var labelOnBottom by DictionaryDelegates.booleanFrom(dictionary)

        var textSize by DictionaryDelegates.doubleFrom(dictionary)
        var iconSize by DictionaryDelegates.doubleFrom(dictionary)
        private var scrollPositionX by DictionaryDelegates.doubleFrom(dictionary)
        private var scrollPositionY by DictionaryDelegates.doubleFrom(dictionary)
        var scrollPosition by CompositeDelegates.doublePoint(::scrollPositionX, ::scrollPositionY)

        init {
            // TODO: What other default values?
            viewOptionsVersion = 1
        }

        fun build(): IconViewOptions {
            return IconViewOptions(dictionary.clone())
        }
    }
}