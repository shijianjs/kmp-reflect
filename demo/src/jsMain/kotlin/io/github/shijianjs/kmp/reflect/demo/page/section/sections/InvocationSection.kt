package io.github.shijianjs.kmp.reflect.demo.page.section.sections

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.kmp.reflect.KmpClass
import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.demo.model.Person
import io.github.shijianjs.kmp.reflect.demo.page.PageBlock
import io.github.shijianjs.kmp.reflect.demo.page.lang.Translation
import io.github.shijianjs.kmp.reflect.demo.page.lang.english
import io.github.shijianjs.kmp.reflect.demo.page.section.OutputList
import io.github.shijianjs.kmp.reflect.demo.page.section.OUTPUT_SHEET
import io.github.shijianjs.kmp.reflect.demo.style.RESET
import web.components.CustomElementReference
import web.dom.TagName

/**
 * `<kmp-invocation>`: constructs a `Person` and calls its members through the metadata, once.
 *
 * The values are fixed by the time the section is built; only their labels depend on the language, so
 * [applyTranslation] rewrites the lines instead of repeating the reflective calls.
 */
internal class InvocationSection : CustomElementAdapter(), PageBlock {

    private val list = OutputList()

    /** `null` when the metadata is not registered, which also means there is nothing to show. */
    private val constructed: String?
    private val greetResult: String?
    private val ageAfterBirthday: String?

    init {
        val shadow = attachStyledShadow(RESET, OUTPUT_SHEET)
        shadow.appendChild(list.element)

        val personClass: KmpClass? =
            KmpClasses.byNameOrNull("io.github.shijianjs.kmp.reflect.demo.model.Person")
        val person = personClass?.constructor(3)?.new("Ada", 36, "hidden") as? Person
        if (person == null) {
            constructed = null
            greetResult = null
            ageAfterBirthday = null
        } else {
            constructed = "${person.name} (${person.age})"
            greetResult = personClass.function("greet", 1)?.invoke(person, "Hello") as? String
            val birthday = personClass.function("birthday", 0)?.invoke(person)
            // `age` is read back through its getter: the call above went through `birthday`.
            ageAfterBirthday = "${personClass.property("age")?.get(person)} (birthday -> $birthday)"
        }
        applyTranslation(english)
    }

    override fun applyTranslation(translation: Translation) {
        list.setLines(
            listOfNotNull(
                constructed?.let { value -> "${translation.constructed}: $value" } ?: translation.detailEmpty,
                greetResult?.let { value -> "${translation.called}: $value" },
                ageAfterBirthday?.let { value -> "${translation.propertyValue} age: $value" },
            ),
        )
    }

    companion object : CustomElementReference<InvocationSection>(InvocationSection::class.js) {
        val NAME: TagName<InvocationSection> = TagName("kmp-invocation")
    }
}

/** Defines `<kmp-invocation>` once and returns its tag. */
internal fun invocationSectionTag(): TagName<InvocationSection> {
    defineIfMissing(InvocationSection.NAME, InvocationSection.Companion)
    return InvocationSection.NAME
}
