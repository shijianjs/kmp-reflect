package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.css.cssSheet
import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.kmp.reflect.resource.KmpResource
import io.github.shijianjs.kmp.reflect.resource.KmpResources
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import web.cssom.Border
import web.cssom.CSSStyleSheet
import web.cssom.Cursor
import web.cssom.Display
import web.cssom.LineStyle
import web.cssom.Margin
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

    private val list = OutputList()

    private val entries: List<Entry>

    init {
        entries = KmpResources.all().sortedBy { resource -> resource.path }.map(::entryOf)
        entries.forEach { entry -> list.element.appendChild(entry.item) }
        shell.body.appendChild(list.element)
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
            classList.add(CODE_CLASS)
        }
        val item = HtmlTagName.li.createElement {
            classList.add(OUTPUT_ITEM_CLASS)
            classList.add(RESOURCE_CLASS)
            appendChild(label)
            if (resource.mimeType.startsWith("image/")) {
                appendChild(imageFor(resource))
            } else {
                appendChild(
                    HtmlTagName.p.createElement {
                        classList.add(PREVIEW_CLASS)
                        classList.add(CODE_CLASS)
                        textContent = resource.text().lines().take(3).joinToString(" / ").trim()
                    },
                )
            }
        }
        return Entry(resource, item, label)
    }

    private fun imageFor(resource: KmpResource): HTMLImageElement =
        HtmlTagName.img.createElement {
            classList.add(THUMB_CLASS)
            alt = resource.fileName
            src = "data:${resource.mimeType};base64,${Base64.encode(resource.bytes)}"
        }
}

/** The resources section's own rules: the label is code, the preview is code and muted, the thumb is framed. */
internal val RESOURCES_SHEET: CSSStyleSheet = cssSheet {
    RESOURCE_CLASS {
        marginBottom = 6.px
    }

    PREVIEW_CLASS {
        margin = Margin(4.px, 0.px, 0.px)
        color = mutedText
        fontSize = 12.px
        lineHeight = number(1.5)
    }

    THUMB_CLASS {
        display = Display.block
        marginTop = 8.px
        maxWidth = 48.px
        border = Border(1.px, LineStyle.solid, hairline)
        borderRadius = 6.px
        cursor = Cursor.default
    }
}
