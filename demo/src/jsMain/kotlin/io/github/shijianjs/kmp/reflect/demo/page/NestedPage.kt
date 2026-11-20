package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.nestedSectionTag

/** Nested classes: the `Outer.Inner` names and their qualified form. */
internal class NestedPage : SectionPage() {
    init {
        show(nestedSectionTag().createElement(), { it.nestedHeading }, { it.nestedHint })
    }

    companion object : CustomElementReferenceAdapter<NestedPage>(NestedPage::class.js, "reflect-nested-page")
}
