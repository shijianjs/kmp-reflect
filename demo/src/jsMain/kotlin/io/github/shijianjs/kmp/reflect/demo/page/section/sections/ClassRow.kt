package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.css.dsl.invoke
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.kmp.reflect.KmpClass
import web.html.HTMLTableCellElement
import web.html.HTMLTableRowElement

/** One reflected class in the table, with the two cells whose text is language-dependent. */
internal class ClassRow(
    val kmpClass: KmpClass,
    val rowElement: HTMLTableRowElement,
    val membersCell: HTMLTableCellElement,
    val annotationsCell: HTMLTableCellElement,
)
