package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.page.PageBlock
import io.github.shijianjs.kmp.reflect.demo.page.lang.Translation
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.section.OutputList
import io.github.shijianjs.kmp.reflect.demo.page.section.OUTPUT_SHEET
import io.github.shijianjs.kmp.reflect.demo.style.RESET
import web.dom.TagName

/** `<kmp-nested>`: nested classes that were themselves reflected, listed under their outer class. */
internal class NestedSection : CustomElementAdapter(), PageBlock {

    private val list = OutputList()

    private val lines: List<String>

    init {
        val shadow = attachStyledShadow(RESET, OUTPUT_SHEET)
        shadow.appendChild(list.element)

        val library = KmpClasses.byNameOrNull("io.github.shijianjs.kmp.reflect.demo.model.Library")
        lines = library?.nestedClasses.orEmpty().map { nested -> "${library?.simpleName} -> ${nested.qualifiedName}" }
        applyTranslation(english)
    }

    override fun applyTranslation(translation: Translation) {
        list.setLines(lines.ifEmpty { listOf(translation.detailEmpty) })
    }

    companion object : CustomElementReferenceAdapter<NestedSection>(NestedSection::class.js, "kmp-nested")
}

/** Defines `<kmp-nested>` once and returns its tag. */
internal fun nestedSectionTag(): TagName<NestedSection> {
    defineIfMissing(NestedSection.NAME, NestedSection.Companion)
    return NestedSection.NAME
}
