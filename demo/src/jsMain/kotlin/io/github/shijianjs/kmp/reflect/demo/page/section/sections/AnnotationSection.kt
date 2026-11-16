package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.page.lang.Copy
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.section.OutputList
import io.github.shijianjs.kmp.reflect.demo.page.section.OUTPUT_SHEET
import io.github.shijianjs.kmp.reflect.demo.page.section.SECTION_SHEET
import io.github.shijianjs.kmp.reflect.demo.page.section.SectionShell
import io.github.shijianjs.kmp.reflect.demo.page.theme.RESET
import web.components.CustomElementReference
import web.dom.TagName

/**
 * `<kmp-annotations>`: classes carrying `@KmpRecord`, with the annotation's arguments from metadata.
 *
 * Matching happens by qualified name, so this section also demonstrates that annotation scanning
 * needs no runtime reflection at all.
 */
internal class AnnotationSection : CustomElementAdapter() {
    private val shell = SectionShell()

    private val list = OutputList()

    private val lines: List<String>

    init {
        val shadow = attachStyledShadow(RESET, SECTION_SHEET, OUTPUT_SHEET)
        shell.nodes.forEach { node -> shadow.appendChild(node) }
        shell.body.appendChild(list.element)

        val qualifiedName = "io.github.shijianjs.kmp.reflect.demo.model.KmpRecord"
        lines = KmpClasses.annotatedWith(qualifiedName).map { kmpClass ->
            val arguments = kmpClass.annotation(qualifiedName)?.arguments
                ?.entries
                ?.joinToString { (name, value) -> "$name=$value" }
                .orEmpty()
            "${kmpClass.simpleName}: $arguments"
        }
        applyCopy(english)
    }

    fun applyCopy(copy: Copy) {
        shell.setCopy(copy.annotationHeading, copy.annotationHint)
        list.setLines(lines.ifEmpty { listOf(copy.noAnnotation) })
    }

    companion object : CustomElementReference<AnnotationSection>(AnnotationSection::class.js) {
        val NAME: TagName<AnnotationSection> = TagName("kmp-annotations")
    }
}

/** Defines `<kmp-annotations>` once and returns its tag. */
internal fun annotationSectionTag(): TagName<AnnotationSection> {
    defineIfMissing(AnnotationSection.NAME, AnnotationSection.Companion)
    return AnnotationSection.NAME
}
