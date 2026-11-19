package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.css.cssSheet
import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.kmp.reflect.demo.page.PageBlock
import io.github.shijianjs.kmp.reflect.demo.page.lang.Translation
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.section.OutputList
import io.github.shijianjs.kmp.reflect.demo.page.section.OUTPUT_SHEET
import io.github.shijianjs.kmp.reflect.demo.style.CODE_CLASS
import io.github.shijianjs.kmp.reflect.demo.style.OUTPUT_ITEM_CLASS
import io.github.shijianjs.kmp.reflect.demo.style.RESET
import io.github.shijianjs.kmp.reflect.demo.style.hairline
import io.github.shijianjs.kmp.reflect.demo.style.mutedText
import io.github.shijianjs.kmp.reflect.resource.KmpResource
import io.github.shijianjs.kmp.reflect.resource.KmpResources
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import web.components.CustomElementReference
import web.cssom.Border
import web.cssom.CSSStyleSheet
import web.cssom.ClassName
import web.cssom.Cursor
import web.cssom.Display
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.number
import web.cssom.px
import web.dom.TagName
import web.html.HTMLImageElement
import web.html.HTMLElement
import web.html.HtmlTagName

/** The resource list's own class names; `CODE_CLASS` / `OUTPUT_ITEM_CLASS` are shared, so they stay in `theme/Classes.kt`. */
private val RESOURCE_CLASS: ClassName = ClassName("resource")

private val PREVIEW_CLASS: ClassName = ClassName("preview")

private val THUMB_CLASS: ClassName = ClassName("thumb")

/**
 * `<kmp-resources>`: the resources the processor read from `demo/data` at compile time.
 *
 * Everything about a resource — path, MIME type, byte count — comes from the metadata; the language
 * only supplies the word for "bytes", so [applyTranslation] rewrites the label of nodes that keep their
 * images and previews.
 */
@OptIn(ExperimentalEncodingApi::class)
internal class ResourcesSection : CustomElementAdapter(), PageBlock {

    private val list = OutputList()

    private val entries: List<Entry>

    init {
        val shadow = attachStyledShadow(RESET, OUTPUT_SHEET, RESOURCES_SHEET)
        shadow.appendChild(list.element)

        entries = KmpResources.all().sortedBy { resource -> resource.path }.map(::entryOf)
        entries.forEach { entry -> list.element.appendChild(entry.item) }
        applyTranslation(english)
    }

    override fun applyTranslation(translation: Translation) {
        entries.forEach { entry ->
            entry.label.textContent =
                "${entry.resource.path} — ${entry.resource.mimeType}, ${entry.resource.size} ${translation.resourceBytes}"
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

    companion object : CustomElementReference<ResourcesSection>(ResourcesSection::class.js) {
        val NAME: TagName<ResourcesSection> = TagName("kmp-resources")

        /** The resources section's own rules: the preview is muted code, the thumb is a framed image. */
        private val RESOURCES_SHEET: CSSStyleSheet = cssSheet {
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
    }
}

/** Defines `<kmp-resources>` once and returns its tag. */
internal fun resourcesSectionTag(): TagName<ResourcesSection> {
    defineIfMissing(ResourcesSection.NAME, ResourcesSection.Companion)
    return ResourcesSection.NAME
}
