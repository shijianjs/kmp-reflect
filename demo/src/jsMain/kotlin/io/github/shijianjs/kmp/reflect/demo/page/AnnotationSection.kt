package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.kmp.reflect.KmpClasses
import web.html.HTMLElement

/**
 * Classes carrying `@KmpRecord`, with the annotation's arguments read from the metadata.
 *
 * Matching happens by qualified name, so this section also demonstrates that annotation scanning
 * needs no runtime reflection at all.
 */
internal class AnnotationSection {
    private val shell = SectionShell()

    val root: HTMLElement = shell.root

    private val list = OutputList()

    private val lines: List<String>

    init {
        val qualifiedName = "io.github.shijianjs.kmp.reflect.demo.model.KmpRecord"
        lines = KmpClasses.annotatedWith(qualifiedName).map { kmpClass ->
            val arguments = kmpClass.annotation(qualifiedName)?.arguments
                ?.entries
                ?.joinToString { (name, value) -> "$name=$value" }
                .orEmpty()
            "${kmpClass.simpleName}: $arguments"
        }
        shell.body.appendChild(list.element)
        applyCopy(english)
    }

    fun applyCopy(copy: Copy) {
        shell.setCopy(copy.annotationHeading, copy.annotationHint)
        list.setLines(lines.ifEmpty { listOf(copy.noAnnotation) })
    }
}
