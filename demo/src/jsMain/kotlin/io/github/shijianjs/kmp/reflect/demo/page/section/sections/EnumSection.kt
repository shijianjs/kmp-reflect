package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.model.Shape
import io.github.shijianjs.kmp.reflect.demo.page.PageBlock
import io.github.shijianjs.kmp.reflect.demo.page.lang.Translation
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.section.OutputList
import io.github.shijianjs.kmp.reflect.demo.style.RESET
import web.dom.TagName

/** `<kmp-enums>`: each enum entry, its ordinal and the real instance behind it. */
internal class EnumSection : CustomElementAdapter(), PageBlock {

    private val list = OutputList()

    private val lines: List<String>

    init {
        val shadow = attachStyledShadow(RESET, OutputList.SHEET)
        shadow.appendChild(list.element)

        val shapeClass = KmpClasses.byNameOrNull("io.github.shijianjs.kmp.reflect.demo.model.Shape")
        lines = shapeClass?.enumEntries.orEmpty().map { entry ->
            val sides = (entry.instance as? Shape)?.sides
            "${entry.name} (${entry.ordinal}) = $sides sides"
        }
        applyTranslation(english)
    }

    override fun applyTranslation(translation: Translation) {
        list.setLines(lines.ifEmpty { listOf(translation.detailEmpty) })
    }

    companion object : CustomElementReferenceAdapter<EnumSection>(EnumSection::class.js, "kmp-enums")
}

