package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.model.Shape
import web.html.HTMLElement

/** Each enum entry, its ordinal and the real instance behind it. */
internal class EnumSection {
    private val shell = SectionShell()

    val root: HTMLElement = shell.root

    private val list = OutputList()

    private val lines: List<String>

    init {
        val shapeClass = KmpClasses.byNameOrNull("io.github.shijianjs.kmp.reflect.demo.model.Shape")
        lines = shapeClass?.enumEntries.orEmpty().map { entry ->
            val sides = (entry.instance as? Shape)?.sides
            "${entry.name} (${entry.ordinal}) = $sides sides"
        }
        shell.body.appendChild(list.element)
        applyCopy(english)
    }

    fun applyCopy(copy: Copy) {
        shell.setCopy(copy.enumHeading, copy.enumHint)
        list.setLines(lines.ifEmpty { listOf(copy.detailEmpty) })
    }
}
