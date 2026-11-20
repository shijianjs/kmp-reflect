package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.ResourcesSection

/** Resources: reading packaged files through the reflection API. */
internal class ResourcesPage : SectionPage() {
    init {
        show(ResourcesSection.NAME.createElement(), { it.resourcesHeading }, { it.resourcesHint })
    }

    companion object : CustomElementReferenceAdapter<ResourcesPage>(ResourcesPage::class.js, "reflect-resources-page")
}
