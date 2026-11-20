package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.InvocationSection

/** Invocation: call a function by name and read what the proxy returned. */
internal class InvocationPage : SectionPage() {
    init {
        show(InvocationSection.NAME.createElement(), { it.invocationHeading }, { it.invocationHint })
    }

    companion object : CustomElementReferenceAdapter<InvocationPage>(InvocationPage::class.js, "reflect-invocation-page")
}
