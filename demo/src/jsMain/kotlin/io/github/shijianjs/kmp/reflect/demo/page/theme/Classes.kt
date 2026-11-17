package io.github.shijianjs.kmp.reflect.demo.page.theme

import web.cssom.ClassName

/**
 * The class names two or more files must agree on.
 *
 * A class name is only shared when the rule that selects it and the node that carries it live in
 * different files — the hint a section's shell styles and the class browser reuses, the code face both
 * the table and the resource list show, the list item the resource section extends. A name used by a
 * single file is declared in that file (a file-private `ClassName` next to its sheet), because that is
 * where both sides of it are: one place to read, one place to change.
 */
internal val HINT_CLASS: ClassName = ClassName("hint")

internal val DETAIL_HINT_CLASS: ClassName = ClassName("detail-hint")

internal val OUTPUT_ITEM_CLASS: ClassName = ClassName("output-item")

internal val CODE_CLASS: ClassName = ClassName("code")
