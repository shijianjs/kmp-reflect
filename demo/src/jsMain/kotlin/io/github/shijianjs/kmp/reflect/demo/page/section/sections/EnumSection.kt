package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.model.Shape
import io.github.shijianjs.kmp.reflect.demo.page.lang.Copy
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.section.OutputList
import io.github.shijianjs.kmp.reflect.demo.page.section.OUTPUT_SHEET
import io.github.shijianjs.kmp.reflect.demo.page.section.SECTION_SHEET
import io.github.shijianjs.kmp.reflect.demo.page.section.SectionShell
import io.github.shijianjs.kmp.reflect.demo.page.theme.RESET
import web.components.CustomElementReference
import web.dom.TagName

/** `<kmp-enums>`: each enum entry, its ordinal and the real instance behind it. */
internal class EnumSection : CustomElementAdapter() {
    private val shell = SectionShell()

    private val list = OutputList()

    private val lines: List<String>

    init {
        val shadow = attachStyledShadow(RESET, SECTION_SHEET, OUTPUT_SHEET)
        shell.nodes.forEach { node -> shadow.appendChild(node) }
        shell.body.appendChild(list.element)

        val shapeClass = KmpClasses.byNameOrNull("io.github.shijianjs.kmp.reflect.demo.model.Shape")
        lines = shapeClass?.enumEntries.orEmpty().map { entry ->
            val sides = (entry.instance as? Shape)?.sides
            "${entry.name} (${entry.ordinal}) = $sides sides"
        }
        applyCopy(english)
    }

    fun applyCopy(copy: Copy) {
        shell.setCopy(copy.enumHeading, copy.enumHint)
        list.setLines(lines.ifEmpty { listOf(copy.detailEmpty) })
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
