package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.ClassBrowser

/*
 * The eight routes of this site. They share one shape — a [SectionPage] frame around one section
 * element — so each one is still its own class and its own tag: the route owns the page, and the
 * section element it hosts owns the demo.
 */

/** Class browser: search a class, then walk its members. */
internal class ClassesPage : SectionPage() {
    init {
        show(ClassBrowser.NAME.createElement(), { it.classesHeading }, { it.classesHint })
    }

    companion object : CustomElementReferenceAdapter<ClassesPage>(ClassesPage::class.js, "reflect-classes-page")
}
