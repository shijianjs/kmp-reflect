package io.github.shijianjs.kmp.reflect.demo.page.section

import io.github.shijianjs.ktjs.dom.css.cssSheet
import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.kmp.reflect.demo.page.theme.DETAIL_HINT_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.HINT_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.SECTION_HEADING_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.SECTION_HINT_CLASS
import io.github.shijianjs.kmp.reflect.demo.page.theme.bodyText
import io.github.shijianjs.kmp.reflect.demo.page.theme.mutedText
import web.cssom.CSSStyleSheet
import web.cssom.Display
import web.cssom.Margin
import web.cssom.number
import web.cssom.px
import web.html.HTMLDivElement
import web.html.HTMLHeadingElement
import web.html.HTMLParagraphElement
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * The nodes every section starts with: a heading, one line of hint, then the body below.
 *
 * Composition, not a base class: each section is its own custom element with its own shadow root and
 * creates one of these inside it. Keeping this one level above the sections is what makes the
 * one-to-many relation visible — eight sections use it, none of them extends it.
 */
internal class SectionShell {
    private val headingElement: HTMLHeadingElement = HtmlTagName.h2.createElement {
        classList.add(SECTION_HEADING_CLASS)
    }

    private val hintElement: HTMLParagraphElement = HtmlTagName.p.createElement {
        classList.add(HINT_CLASS)
        classList.add(SECTION_HINT_CLASS)
    }

    val body: HTMLDivElement = HtmlTagName.div.createElement()

    /** The nodes in render order; a section appends them to its shadow root as they are. */
    val nodes: List<HTMLElement> = listOf(headingElement, hintElement, body)

    /** The text a section shares with its peers: a heading and its one-line explanation. */
    fun setCopy(heading: String, hint: String) {
        headingElement.textContent = heading
        hintElement.textContent = hint
    }
}

/**
 * The rules every section's shadow root adopts, after [io.github.shijianjs.ktjs.dom.css]'s reset.
 *
 * `:host` carries the section's own box (the 36px gap between sections), because the host element is
 * what the page lays out; the rest is the heading/hint typography all sections share. A section's own
 * rules live in that section's sheet and are adopted on top of this one.
 */
internal val SECTION_SHEET: CSSStyleSheet = cssSheet {
    ":host" {
        display = Display.block
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
