package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.enumSectionTag

/** Enums: entries, ordinals and the members the compiler keeps. */
internal class EnumsPage : SectionPage() {
    init {
        show(enumSectionTag().createElement(), { it.enumHeading }, { it.enumHint })
    }

    companion object : CustomElementReferenceAdapter<EnumsPage>(EnumsPage::class.js, "reflect-enums-page")
}
