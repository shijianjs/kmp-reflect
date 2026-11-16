package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.css.cssSheet
import web.cssom.Auto
import web.cssom.BoxSizing
import web.cssom.CSSStyleSheet
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.FontFamily
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.number
import web.cssom.px
import web.cssom.rem
import web.cssom.rgb
import web.dom.document

/**
 * The demo's palette, as typed colours.
 *
 * One place, because the page's sections and their stylesheets both paint with them; a value that
 * several components must change together is exactly what a shared constant is for.
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

/**
 * The class vocabulary shared by the sheets and the components.
 *
 * Every token is declared here once and used on both sides — by the rule that selects it and by the
 * node that carries it — so a typo cannot leave a rule silently matching nothing. The rules
 * themselves stay next to the component that owns them; this file only keeps the vocabulary (tokens
 * several sections need) and the page-level sheet.
 */
internal val PAGE_CLASS: ClassName = ClassName("page")

internal val HEADER_CLASS: ClassName = ClassName("header")

internal val TITLE_CLASS: ClassName = ClassName("title")

internal val SUBTITLE_CLASS: ClassName = ClassName("subtitle")

internal val LANG_CLASS: ClassName = ClassName("lang-switch")

internal val SECTION_CLASS: ClassName = ClassName("section")

internal val SECTION_HEADING_CLASS: ClassName = ClassName("section-heading")

internal val HINT_CLASS: ClassName = ClassName("hint")

internal val SECTION_HINT_CLASS: ClassName = ClassName("section-hint")

internal val DETAIL_HINT_CLASS: ClassName = ClassName("detail-hint")

internal val OUTPUT_CLASS: ClassName = ClassName("output")

internal val OUTPUT_ITEM_CLASS: ClassName = ClassName("output-item")

internal val TABLE_CLASS: ClassName = ClassName("class-table")

internal val CELL_CLASS: ClassName = ClassName("cell")

internal val HEAD_CELL_CLASS: ClassName = ClassName("cell-head")

internal val CODE_CLASS: ClassName = ClassName("code")

/** A selectable table row; [SELECTED_CLASS] is toggled by `ClassBrowser` as rows are picked. */
internal val ROW_CLASS: ClassName = ClassName("row")

internal val SELECTED_CLASS: ClassName = ClassName("is-selected")

internal val DETAIL_CLASS: ClassName = ClassName("detail")

internal val DETAIL_HEADING_CLASS: ClassName = ClassName("detail-heading")

internal val MEMBER_CLASS: ClassName = ClassName("member")

internal val MEMBER_LIST_CLASS: ClassName = ClassName("member-list")

internal val BADGE_CLASS: ClassName = ClassName("badge")

internal val BADGE_OK_CLASS: ClassName = ClassName("badge-ok")

internal val BADGE_MUTED_CLASS: ClassName = ClassName("badge-muted")

internal val RESOURCE_CLASS: ClassName = ClassName("resource")

internal val PREVIEW_CLASS: ClassName = ClassName("preview")

internal val THUMB_CLASS: ClassName = ClassName("thumb")

/**
 * The page-level sheet: the reset and the host element, nothing else.
 *
 * Every other rule lives in the sheet of the component it belongs to (`HeaderBar`, `SectionShell`,
 * `OutputList`, `ClassBrowser`, `ResourcesSection`). They are all adopted once, here, because a plain
 * page has no shadow root to scope them per component; only *runtime* values are ever written inline.
 */
internal val PAGE_SHEET: CSSStyleSheet = cssSheet {
    "*" { boxSizing = BoxSizing.borderBox }

    PAGE_CLASS {
        maxWidth = 62.rem
        margin = Margin(0.px, Auto.auto)
        padding = Padding(32.px, 24.px, 64.px)
        backgroundColor = pageBackground
        color = bodyText
        fontFamily = FontFamily.systemUi
        fontSize = 15.px
        lineHeight = number(1.55)
    }
}

/**
 * Adopts [PAGE_SHEET] and every component's sheet, once, before the first node is built.
 *
 * `adoptedStyleSheets` answers a read-only array; the document reads the JS array it was given, so
 * handing it the previous sheets plus these is what adopts them. Order matters: base first, then the
 * components, so a later rule wins over an earlier one at equal specificity.
 */
internal fun installPageStyle(vararg componentSheets: CSSStyleSheet) {
    document.adoptedStyleSheets = arrayOf(*document.adoptedStyleSheets, PAGE_SHEET, *componentSheets)
}
