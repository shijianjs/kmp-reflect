package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.inlineStyle
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
        inlineStyle.marginTop = 36.px
    }

    private val headingElement: HTMLHeadingElement = HtmlTagName.h2.createElement {
        inlineStyle.apply {
            margin = Margin(0.px, 0.px, 4.px)
            fontSize = 19.px
        }
    }

    private val hintElement: HTMLParagraphElement = HtmlTagName.p.createElement {
        addClass("hint")
        inlineStyle.apply {
            margin = Margin(0.px, 0.px, 14.px)
            color = mutedText
            fontSize = 13.px
            lineHeight = number(1.5)
        }
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

/** The monospace family the member signatures and resource previews use, as a typed value. */
internal val codeFamily: FontFamily = FontFamily.monospace
