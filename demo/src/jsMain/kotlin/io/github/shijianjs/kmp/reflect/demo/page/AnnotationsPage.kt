package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.AnnotationSection

/** Annotations: reading them off a class at runtime. */
internal class AnnotationsPage : SectionPage() {
    init {
        show(AnnotationSection.NAME.createElement(), { it.annotationHeading }, { it.annotationHint })
    }

    companion object : CustomElementReferenceAdapter<AnnotationsPage>(AnnotationsPage::class.js, "reflect-annotations-page")
}
