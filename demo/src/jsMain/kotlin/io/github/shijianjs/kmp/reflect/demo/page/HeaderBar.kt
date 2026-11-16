package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.css.cssSheet
import io.github.shijianjs.ktjs.dom.css.dsl.Pseudos
import io.github.shijianjs.ktjs.dom.css.dsl.invoke
import io.github.shijianjs.ktjs.dom.element.createElement
import web.cssom.AlignItems
import web.cssom.Border
import web.cssom.CSSStyleSheet
import web.cssom.Cursor
import web.cssom.Display
import web.cssom.FlexWrap
import web.cssom.FontWeight
import web.cssom.JustifyContent
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.em
import web.cssom.number
import web.cssom.pct
import web.cssom.px
import web.cssom.rem
import web.events.EventHandler
import web.html.ButtonType
import web.html.button
import web.html.HTMLButtonElement
import web.html.HTMLHeadingElement
import web.html.HTMLParagraphElement
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * Title, one-line description and the language button.
 *
 * The button's label says which language it switches *to*, which is why it is one of the nodes
 * [applyCopy] rewrites; the click itself only reports that the page should switch.
 */
internal class HeaderBar(private val onSwitch: () -> Unit) {
    private val titleElement: HTMLHeadingElement = HtmlTagName.h1.createElement {
        classList.add(TITLE_CLASS)
    }

    private val subtitleElement: HTMLParagraphElement = HtmlTagName.p.createElement {
        classList.add(SUBTITLE_CLASS)
    }

    private val switchButton: HTMLButtonElement = HtmlTagName.button.createElement {
        classList.add(LANG_CLASS)
        type = ButtonType.button
        onclick = EventHandler { onSwitch() }
    }

    private val titles: HTMLElement = HtmlTagName.div.createElement {
        appendChild(titleElement)
        appendChild(subtitleElement)
    }

    val root: HTMLElement = HtmlTagName.div.createElement {
        classList.add(HEADER_CLASS)
        appendChild(titles)
        appendChild(switchButton)
    }

    fun applyCopy(copy: Copy) {
        titleElement.textContent = copy.title
        subtitleElement.textContent = copy.subtitle
        switchButton.textContent = copy.switchLabel
    }
}

/**
 * The header's own rules.
 *
 * The button's background, border and text colour live here rather than inline: a `<button>` otherwise
 * keeps the UA's `buttontext` colour (invisible on this background), and an inline colour would win
 * over the `:hover` rule below.
 */
internal val HEADER_SHEET: CSSStyleSheet = cssSheet {
    HEADER_CLASS {
        display = Display.flex
        alignItems = AlignItems.flexStart
        justifyContent = JustifyContent.spaceBetween
        rowGap = 0.75.rem
        flexWrap = FlexWrap.wrap
        paddingBottom = 20.px
        borderBottomWidth = 1.px
        borderBottomStyle = LineStyle.solid
        borderBottomColor = hairline
    }

    TITLE_CLASS {
        margin = Margin(0.px, 0.px)
        fontSize = 30.px
        letterSpacing = (-0.02).em
        color = bodyText
    }

    SUBTITLE_CLASS {
        margin = Margin(6.px, 0.px, 0.px)
        color = mutedText
        fontSize = 14.px
        lineHeight = number(1.55)
        maxWidth = 62.pct
    }

    LANG_CLASS {
        padding = Padding(6.px, 14.px)
        backgroundColor = panel
        border = Border(1.px, LineStyle.solid, hairline)
        borderRadius = 999.px
        color = mutedText
        fontSize = 12.px
        fontWeight = FontWeight.bold
        cursor = Cursor.pointer

        Pseudos.hover {
            borderColor = accent
            color = accent
        }
    }
}
