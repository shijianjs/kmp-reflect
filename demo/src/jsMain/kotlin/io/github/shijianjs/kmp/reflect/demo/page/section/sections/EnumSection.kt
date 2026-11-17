package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.model.Shape
import io.github.shijianjs.kmp.reflect.demo.page.PageBlock
import io.github.shijianjs.kmp.reflect.demo.page.lang.Translation
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.section.OutputList
import io.github.shijianjs.kmp.reflect.demo.page.section.OUTPUT_SHEET
import io.github.shijianjs.kmp.reflect.demo.page.section.sectionShellTag
import io.github.shijianjs.kmp.reflect.demo.page.theme.RESET
import web.components.CustomElementReference
import web.dom.TagName

/** `<kmp-enums>`: each enum entry, its ordinal and the real instance behind it. */
internal class EnumSection : CustomElementAdapter(), PageBlock {
    private val shell = sectionShellTag().createElement()

    private val list = OutputList()

    private val lines: List<String>

    init {
        val shadow = attachStyledShadow(RESET, OUTPUT_SHEET)
        shadow.appendChild(shell)
        shell.appendChild(list.element)

        val shapeClass = KmpClasses.byNameOrNull("io.github.shijianjs.kmp.reflect.demo.model.Shape")
        lines = shapeClass?.enumEntries.orEmpty().map { entry ->
            val sides = (entry.instance as? Shape)?.sides
            "${entry.name} (${entry.ordinal}) = $sides sides"
        }
        applyTranslation(english)
    }

    override fun applyTranslation(translation: Translation) {
        shell.setText(translation.enumHeading, translation.enumHint)
        list.setLines(lines.ifEmpty { listOf(translation.detailEmpty) })
    }

    companion object : CustomElementReference<EnumSection>(EnumSection::class.js) {
        val NAME: TagName<EnumSection> = TagName("kmp-enums")
    }
}

/** Defines `<kmp-enums>` once and returns its tag. */
internal fun enumSectionTag(): TagName<EnumSection> {
    defineIfMissing(EnumSection.NAME, EnumSection.Companion)
    return EnumSection.NAME
}
