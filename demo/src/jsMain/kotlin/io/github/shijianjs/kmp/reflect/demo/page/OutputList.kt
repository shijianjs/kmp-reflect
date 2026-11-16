package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.css.cssSheet
import io.github.shijianjs.ktjs.dom.element.createElement
import web.cssom.CSSStyleSheet
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.number
import web.cssom.px
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * A list of plain lines, all of which change together when the language does.
 *
 * The list is one local collection whose whole content is language-dependent, so replacing its
 * children is the intended update: no line survives a copy change, and nothing above it is touched.
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
}

/** The rules of the output list: one rule per node kind, however many lines the list ends up with. */
internal val OUTPUT_SHEET: CSSStyleSheet = cssSheet {
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
