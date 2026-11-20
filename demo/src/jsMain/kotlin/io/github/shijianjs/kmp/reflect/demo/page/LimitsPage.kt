package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.limitationsSectionTag

/** Limitations: what the wrapper cannot do on the JS/Native targets. */
internal class LimitsPage : SectionPage() {
    init {
        show(limitationsSectionTag().createElement(), { it.limitationsHeading }, { "" })
    }

    companion object : CustomElementReferenceAdapter<LimitsPage>(LimitsPage::class.js, "reflect-limits-page")
}
