package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.annotationSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.classBrowserTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.enumSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.invocationSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.limitationsSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.nestedSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.proxySectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.resourcesSectionTag

/*
 * The eight routes of this site. They share one shape — a [SectionPage] frame around one section
 * element — so each one is still its own class and its own tag: the route owns the page, and the
 * section element it hosts owns the demo.
 */

/** Class browser: search a class, then walk its members. */
internal class ClassesPage : SectionPage() {
    init {
        show(classBrowserTag().createElement(), { it.classesHeading }, { it.classesHint })
    }

    companion object : CustomElementReferenceAdapter<ClassesPage>(ClassesPage::class.js, "reflect-classes-page")
}

/** Invocation: call a function by name and read what the proxy returned. */
internal class InvocationPage : SectionPage() {
    init {
        show(invocationSectionTag().createElement(), { it.invocationHeading }, { it.invocationHint })
    }

    companion object : CustomElementReferenceAdapter<InvocationPage>(InvocationPage::class.js, "reflect-invocation-page")
}

/** Proxies: what `kmp-reflect` hands back for a class it cannot see at compile time. */
internal class ProxyPage : SectionPage() {
    init {
        show(proxySectionTag().createElement(), { it.proxyHeading }, { it.proxyHint })
    }

    companion object : CustomElementReferenceAdapter<ProxyPage>(ProxyPage::class.js, "reflect-proxy-page")
}

/** Resources: reading packaged files through the reflection API. */
internal class ResourcesPage : SectionPage() {
    init {
        show(resourcesSectionTag().createElement(), { it.resourcesHeading }, { it.resourcesHint })
    }

    companion object : CustomElementReferenceAdapter<ResourcesPage>(ResourcesPage::class.js, "reflect-resources-page")
}

/** Annotations: reading them off a class at runtime. */
internal class AnnotationsPage : SectionPage() {
    init {
        show(annotationSectionTag().createElement(), { it.annotationHeading }, { it.annotationHint })
    }

    companion object : CustomElementReferenceAdapter<AnnotationsPage>(AnnotationsPage::class.js, "reflect-annotations-page")
}

/** Enums: entries, ordinals and the members the compiler keeps. */
internal class EnumsPage : SectionPage() {
    init {
        show(enumSectionTag().createElement(), { it.enumHeading }, { it.enumHint })
    }

    companion object : CustomElementReferenceAdapter<EnumsPage>(EnumsPage::class.js, "reflect-enums-page")
}

/** Nested classes: the `Outer.Inner` names and their qualified form. */
internal class NestedPage : SectionPage() {
    init {
        show(nestedSectionTag().createElement(), { it.nestedHeading }, { it.nestedHint })
    }

    companion object : CustomElementReferenceAdapter<NestedPage>(NestedPage::class.js, "reflect-nested-page")
}

/** Limitations: what the wrapper cannot do on the JS/Native targets. */
internal class LimitsPage : SectionPage() {
    init {
        show(limitationsSectionTag().createElement(), { it.limitationsHeading }, { "" })
    }

    companion object : CustomElementReferenceAdapter<LimitsPage>(LimitsPage::class.js, "reflect-limits-page")
}
