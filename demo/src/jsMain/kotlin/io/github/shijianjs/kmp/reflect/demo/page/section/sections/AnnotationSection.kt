package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.page.PageBlock
import io.github.shijianjs.kmp.reflect.demo.page.lang.Translation
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.section.OutputList
import io.github.shijianjs.kmp.reflect.demo.page.section.OUTPUT_SHEET
import io.github.shijianjs.kmp.reflect.demo.page.section.sectionShellTag
import io.github.shijianjs.kmp.reflect.demo.page.theme.RESET
import web.components.CustomElementReference
import web.dom.TagName

/**
 * `<kmp-annotations>`: classes carrying `@KmpRecord`, with the annotation's arguments from metadata.
 *
 * Matching happens by qualified name, so this section also demonstrates that annotation scanning
 * needs no runtime reflection at all.
 */
internal class AnnotationSection : CustomElementAdapter(), PageBlock {
    private val shell = sectionShellTag().createElement()

    private val list = OutputList()

    private val lines: List<String>

    init {
        val shadow = attachStyledShadow(RESET, OUTPUT_SHEET)
        shadow.appendChild(shell)
        shell.appendChild(list.element)

        val qualifiedName = "io.github.shijianjs.kmp.reflect.demo.model.KmpRecord"
        lines = KmpClasses.annotatedWith(qualifiedName).map { kmpClass ->
            val arguments = kmpClass.annotation(qualifiedName)?.arguments
                ?.entries
                ?.joinToString { (name, value) -> "$name=$value" }
                .orEmpty()
            "${kmpClass.simpleName}: $arguments"
        }
        applyTranslation(english)
    }

    override fun applyTranslation(translation: Translation) {
        shell.setText(translation.annotationHeading, translation.annotationHint)
        list.setLines(lines.ifEmpty { listOf(translation.noAnnotation) })
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
