package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.inlineStyle
import web.cssom.AlignItems
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
        inlineStyle.apply {
            margin = Margin(0.px, 0.px)
            fontSize = 30.px
            letterSpacing = (-0.02).em
        }
    }

    private val subtitleElement: HTMLParagraphElement = HtmlTagName.p.createElement {
        classList.add(SUBTITLE_CLASS)
        inlineStyle.apply {
            margin = Margin(6.px, 0.px, 0.px)
            color = mutedText
            fontSize = 14.px
            lineHeight = number(1.55)
            maxWidth = 62.pct
        }
    }

    private val switchButton: HTMLButtonElement = HtmlTagName.button.createElement {
        classList.add(LANG_CLASS)
        type = ButtonType.button
        inlineStyle.apply {
            // Border colour, text colour and the background they sit on change on hover, so all three
            // live in PAGE_SHEET; everything else is inline.
            padding = Padding(6.px, 14.px)
            borderWidth = 1.px
            borderStyle = LineStyle.solid
            borderRadius = 999.px
            fontSize = 12.px
            fontWeight = FontWeight.bold
            cursor = Cursor.pointer
        }
        onclick = EventHandler { onSwitch() }
    }

    private val titles: HTMLElement = HtmlTagName.div.createElement {
        appendChild(titleElement)
        appendChild(subtitleElement)
    }

    val root: HTMLElement = HtmlTagName.div.createElement {
        classList.add(HEADER_CLASS)
        inlineStyle.apply {
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
        appendChild(titles)
        appendChild(switchButton)
    }

    fun applyCopy(copy: Copy) {
        titleElement.textContent = copy.title
        subtitleElement.textContent = copy.subtitle
        switchButton.textContent = copy.switchLabel
    }
}
