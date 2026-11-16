package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.css.cssSheet
import io.github.shijianjs.ktjs.dom.css.dsl.Pseudos
import io.github.shijianjs.ktjs.dom.css.dsl.invoke
import web.cssom.BoxSizing
import web.cssom.CSSStyleSheet
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.rgb
import web.dom.document

/**
 * The demo's palette, as typed colours.
 *
 * One place, because the page's sections and the one document-level sheet both paint with them; a
 * value that several components must change together is exactly what a shared constant is for.
 */
internal val pageBackground: Color = rgb(15, 17, 21)

internal val panel: Color = rgb(23, 26, 33)

internal val panelStrong: Color = rgb(30, 34, 43)

internal val bodyText: Color = rgb(229, 231, 235)

internal val mutedText: Color = rgb(154, 163, 175)

internal val accent: Color = rgb(110, 168, 254)

internal val okText: Color = rgb(74, 222, 128)

internal val hairline: Color = rgb(42, 47, 58)

internal val selectedFill: Color = rgb(110, 168, 254, 0.12)

internal val okFill: Color = rgb(74, 222, 128, 0.12)

internal val mutedFill: Color = rgb(154, 163, 175, 0.12)

/** A selectable table row; [SELECTED_CLASS] is toggled by `ClassBrowser` as rows are picked. */
internal val ROW_CLASS: ClassName = ClassName("row")

internal val SELECTED_CLASS: ClassName = ClassName("is-selected")

internal val LANG_CLASS: ClassName = ClassName("lang-switch")

/**
 * The other class tokens the page puts on nodes.
 *
 * They live here for the same reason the colours do: [PAGE_SHEET] has to name them, and the
 * components that mark their nodes have to name them. A plain string at either end would silently
 * drift from the other, so every token is one typed constant used by both sides.
 */
internal val HEADER_CLASS: ClassName = ClassName("header")

internal val TITLE_CLASS: ClassName = ClassName("title")

internal val SUBTITLE_CLASS: ClassName = ClassName("subtitle")

internal val HINT_CLASS: ClassName = ClassName("hint")

internal val DETAIL_CLASS: ClassName = ClassName("detail")

internal val MEMBER_CLASS: ClassName = ClassName("member")

internal val OUTPUT_CLASS: ClassName = ClassName("output")

internal val RESOURCE_CLASS: ClassName = ClassName("resource")

internal val PREVIEW_CLASS: ClassName = ClassName("preview")

internal val THUMB_CLASS: ClassName = ClassName("thumb")

/**
 * The one stylesheet the page adopts on the document.
 *
 * Only what an inline declaration cannot say lives here: `:hover` on the two interactive kinds of
 * node (an inline background would win over the hover rule, so both stay in the sheet), the base
 * text colour of the language button (its `:hover` colour is set here too, and an inline colour
 * would shadow it), and the `box-sizing` reset the page needs before any component styles a padded
 * box. Everything else is inline, next to the node that owns it.
 */
internal val PAGE_SHEET: CSSStyleSheet = cssSheet {
    "*" { boxSizing = BoxSizing.borderBox }

    ROW_CLASS {
        Pseudos.hover { backgroundColor = panelStrong }
    }

    // CSS has no typed way to join two `ClassName`s, so the compound selector is spelled once here,
    // next to the two tokens it is made of — nowhere else in the page knows it.
    ".row.is-selected" { backgroundColor = selectedFill }

    LANG_CLASS {
        // A `<button>` starts with the UA's own `buttontext` colour, which is unreadable on this
        // background; the sheet, not an inline declaration, so `:hover` can still override it. The
        // background and border colour are here for the same reason — inline would win over `:hover`.
        color = mutedText
        backgroundColor = panel
        borderColor = hairline

        Pseudos.hover {
            borderColor = accent
            color = accent
        }
    }
}

/** Adopts [PAGE_SHEET] once, before the first node is built. */
internal fun installPageStyle() {
    // `adoptedStyleSheets` answers a read-only array; the document reads the JS array it was given,
    // so handing it the previous sheets plus this one is what adopts it.
    document.adoptedStyleSheets = arrayOf(*document.adoptedStyleSheets, PAGE_SHEET)
}
