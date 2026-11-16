package io.github.shijianjs.kmp.reflect.demo.page.theme

import web.cssom.ClassName

/**
 * The class vocabulary the page's sheets and nodes share.
 *
 * Each token is declared here once and used on both sides — by the rule that selects it and by the
 * node that carries it — so a typo cannot leave a rule silently matching nothing. Every component
 * renders into its own shadow root, so these names never meet each other in one tree; they are
 * shared as *vocabulary*, not as a scope.
 */
internal val TITLE_CLASS: ClassName = ClassName("title")

internal val SUBTITLE_CLASS: ClassName = ClassName("subtitle")

internal val LANG_CLASS: ClassName = ClassName("lang-switch")

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

/** A selectable table row; [SELECTED_CLASS] is toggled by the class browser as rows are picked. */
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
