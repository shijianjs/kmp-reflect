package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.css.cssSheet
import io.github.shijianjs.ktjs.dom.element.createElement
import web.cssom.CSSStyleSheet
import web.cssom.FontFamily
import web.cssom.Margin
import web.cssom.number
import web.cssom.px
import web.html.HTMLDivElement
import web.html.HTMLHeadingElement
import web.html.HTMLParagraphElement
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * A numbered section: heading, one line of hint, then the body a section fills in.
 *
 * Not a component base class — it owns three nodes and exposes them, so each section keeps its own
 * class and its own DOM, and this only removes the repeated heading/hint construction.
 */
internal class SectionShell {
    val root: HTMLElement = HtmlTagName.section.createElement {
        classList.add(SECTION_CLASS)
    }

    private val headingElement: HTMLHeadingElement = HtmlTagName.h2.createElement {
        classList.add(SECTION_HEADING_CLASS)
    }

    private val hintElement: HTMLParagraphElement = HtmlTagName.p.createElement {
        classList.add(HINT_CLASS)
        classList.add(SECTION_HINT_CLASS)
    }

    val body: HTMLDivElement = HtmlTagName.div.createElement()

    init {
        root.appendChild(headingElement)
        root.appendChild(hintElement)
        root.appendChild(body)
    }

    /** The text two sections share: a heading and its one-line explanation. */
    fun setCopy(heading: String, hint: String) {
        headingElement.textContent = heading
        hintElement.textContent = hint
    }
}

/**
 * The section shell's own rules.
 *
 * `.hint` is the shared part (colour, size, line height); the two margins belong to the two places a
 * hint appears, which is why they are separate modifiers rather than one rule that everyone lives with.
 */
internal val SECTION_SHEET: CSSStyleSheet = cssSheet {
    SECTION_CLASS {
        marginTop = 36.px
    }

    SECTION_HEADING_CLASS {
        margin = Margin(0.px, 0.px, 4.px)
        fontSize = 19.px
        color = bodyText
    }

    HINT_CLASS {
        margin = Margin(0.px, 0.px)
        color = mutedText
        fontSize = 13.px
        lineHeight = number(1.5)
    }

    SECTION_HINT_CLASS {
        marginBottom = 14.px
    }

    DETAIL_HINT_CLASS {
        marginBottom = 0.px
    }
}

/** The monospace family the member signatures and resource previews use, as a typed value. */
internal val codeFamily: FontFamily = FontFamily.monospace
