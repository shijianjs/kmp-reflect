package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.kmp.reflect.KmpClasses
import web.html.HTMLElement

/** Nested classes that were themselves reflected, listed under their outer class. */
internal class NestedSection {
    private val shell = SectionShell()

    val root: HTMLElement = shell.root

    private val list = OutputList()

    private val lines: List<String>

    init {
        val library = KmpClasses.byNameOrNull("io.github.shijianjs.kmp.reflect.demo.model.Library")
        lines = library?.nestedClasses.orEmpty().map { nested -> "${library?.simpleName} -> ${nested.qualifiedName}" }
        shell.body.appendChild(list.element)
        applyCopy(english)
    }

    fun applyCopy(copy: Copy) {
        shell.setCopy(copy.nestedHeading, copy.nestedHint)
        list.setLines(lines.ifEmpty { listOf(copy.detailEmpty) })
    }
}
