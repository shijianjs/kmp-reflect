package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.inlineStyle
import io.github.shijianjs.kmp.reflect.resource.KmpResource
import io.github.shijianjs.kmp.reflect.resource.KmpResources
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import web.cssom.Cursor
import web.cssom.Display
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.number
import web.cssom.px
import web.html.HTMLImageElement
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * The resources the processor read from `demo/data` at compile time.
 *
 * Everything about a resource — path, MIME type, byte count — comes from the metadata; the language
 * only supplies the word for "bytes", so [applyCopy] re-writes the labels of nodes that keep their
 * images and previews.
 */
@OptIn(ExperimentalEncodingApi::class)
internal class ResourcesSection {
    private val shell = SectionShell()

    val root: HTMLElement = shell.root

    private val list = HtmlTagName.ul.createElement {
        addClass("output")
        inlineStyle.apply {
            margin = Margin(0.px, 0.px)
            padding = Padding(0.px, 0.px, 0.px, 18.px)
            fontSize = 13.px
            lineHeight = number(1.6)
        }
    }

    private val entries: List<Entry>

    init {
        entries = KmpResources.all().sortedBy { resource -> resource.path }.map(::entryOf)
        entries.forEach { entry -> list.appendChild(entry.item) }
        shell.body.appendChild(list)
        applyCopy(english)
    }

    fun applyCopy(copy: Copy) {
        shell.setCopy(copy.resourcesHeading, copy.resourcesHint)
        entries.forEach { entry ->
            entry.label.textContent =
                "${entry.resource.path} — ${entry.resource.mimeType}, ${entry.resource.size} ${copy.resourceBytes}"
        }
    }

    /** One list item: its label node is rewritten on copy change, the rest stays put. */
    private class Entry(val resource: KmpResource, val item: HTMLElement, val label: HTMLElement)

    private fun entryOf(resource: KmpResource): Entry {
        val label = HtmlTagName.span.createElement {
            inlineStyle.fontFamily = codeFamily
        }
        val item = HtmlTagName.li.createElement {
            addClass("resource")
            inlineStyle.marginBottom = 6.px
            appendChild(label)
            if (resource.mimeType.startsWith("image/")) {
                appendChild(imageFor(resource))
            } else {
                appendChild(
                    HtmlTagName.p.createElement {
                        addClass("preview")
                        inlineStyle.apply {
                            margin = Margin(4.px, 0.px, 0.px)
                            color = mutedText
                            fontSize = 12.px
                            fontFamily = codeFamily
                        }
                        textContent = resource.text().lines().take(3).joinToString(" / ").trim()
                    },
                )
            }
        }
        return Entry(resource, item, label)
    }

    private fun imageFor(resource: KmpResource): HTMLImageElement =
        HtmlTagName.img.createElement {
            addClass("thumb")
            alt = resource.fileName
            src = "data:${resource.mimeType};base64,${Base64.encode(resource.bytes)}"
            inlineStyle.apply {
                display = Display.block
                marginTop = 8.px
                maxWidth = 48.px
                borderRadius = 6.px
                borderWidth = 1.px
                borderStyle = LineStyle.solid
                borderColor = hairline
                cursor = Cursor.default
            }
        }
}
