package io.github.shijianjs.kmp.reflect.demo.page.section

import io.github.shijianjs.ktjs.dom.css.cssSheet
import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.kmp.reflect.demo.style.OUTPUT_ITEM_CLASS
import web.cssom.CSSStyleSheet
import web.cssom.ClassName
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.number
import web.cssom.px
import web.html.HTMLElement
import web.html.HtmlTagName

/** The list's own class name; the item class is shared, so it stays in `theme/Classes.kt`. */
private val OUTPUT_CLASS: ClassName = ClassName("output")

/**
 * A list of plain lines, which is the whole body of most sections.
 *
 * Every line of these lists comes from one language, so a copy change replaces the collection
 * wholesale — that is the case `replaceChildren` is for. The nodes themselves are the section's own;
 * nothing above them is touched.
 */
internal class OutputList {
    val element: HTMLElement = HtmlTagName.ul.createElement {
        classList.add(OUTPUT_CLASS)
    }

    fun setLines(lines: List<String>) {
        val items = lines.map { line ->
            HtmlTagName.li.createElement {
                classList.add(OUTPUT_ITEM_CLASS)
                textContent = line
            }
        }
        element.replaceChildren(*items.toTypedArray())
    }

    companion object {
        /**
         * The rules of any output list, adopted by every section whose body is one.
         *
         * The list is the same shape everywhere, and it must look the same everywhere — that is the
         * promise this shared sheet makes. A section with more than a list (the class browser, the
         * resources) adds its own rules on top instead of copying these.
         *
         * It lives here rather than at file level: the rules belong to the shape they describe, and
         * every section reads them through `OutputList` instead of importing a free-standing value.
         */
        val SHEET: CSSStyleSheet = cssSheet {
            OUTPUT_CLASS {
                margin = Margin(0.px, 0.px)
                padding = Padding(0.px, 0.px, 0.px, 18.px)
                fontSize = 13.px
                lineHeight = number(1.6)
            }

            OUTPUT_ITEM_CLASS {
                marginBottom = 3.px
            }
        }
    }
}
