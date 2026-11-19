package io.github.shijianjs.kmp.reflect.demo.page

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.annotationSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.classBrowserTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.enumSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.invocationSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.limitationsSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.nestedSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.proxySectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.resourcesSectionTag
import web.components.CustomElementReference
import web.dom.TagName

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

    companion object : CustomElementReference<ClassesPage>(ClassesPage::class.js) {
        val NAME: TagName<ClassesPage> = TagName("reflect-classes-page")

        fun define() {
            defineIfMissing(NAME, this)
        }
    }
}

/** Creates a defined `<reflect-classes-page>`. */
internal fun newClassesPage(): ClassesPage {
    ClassesPage.define()
    return ClassesPage.NAME()
}

/** Invocation: call a function by name and read what the proxy returned. */
internal class InvocationPage : SectionPage() {
    init {
        show(invocationSectionTag().createElement(), { it.invocationHeading }, { it.invocationHint })
    }

    companion object : CustomElementReference<InvocationPage>(InvocationPage::class.js) {
        val NAME: TagName<InvocationPage> = TagName("reflect-invocation-page")

        fun define() {
            defineIfMissing(NAME, this)
        }
    }
}

/** Creates a defined `<reflect-invocation-page>`. */
internal fun newInvocationPage(): InvocationPage {
    InvocationPage.define()
    return InvocationPage.NAME()
}

/** Proxies: what `kmp-reflect` hands back for a class it cannot see at compile time. */
internal class ProxyPage : SectionPage() {
    init {
        show(proxySectionTag().createElement(), { it.proxyHeading }, { it.proxyHint })
    }

    companion object : CustomElementReference<ProxyPage>(ProxyPage::class.js) {
        val NAME: TagName<ProxyPage> = TagName("reflect-proxy-page")

        fun define() {
            defineIfMissing(NAME, this)
        }
    }
}

/** Creates a defined `<reflect-proxy-page>`. */
internal fun newProxyPage(): ProxyPage {
    ProxyPage.define()
    return ProxyPage.NAME()
}

/** Resources: reading packaged files through the reflection API. */
internal class ResourcesPage : SectionPage() {
    init {
        show(resourcesSectionTag().createElement(), { it.resourcesHeading }, { it.resourcesHint })
    }

    companion object : CustomElementReference<ResourcesPage>(ResourcesPage::class.js) {
        val NAME: TagName<ResourcesPage> = TagName("reflect-resources-page")

        fun define() {
            defineIfMissing(NAME, this)
        }
    }
}

/** Creates a defined `<reflect-resources-page>`. */
internal fun newResourcesPage(): ResourcesPage {
    ResourcesPage.define()
    return ResourcesPage.NAME()
}

/** Annotations: reading them off a class at runtime. */
internal class AnnotationsPage : SectionPage() {
    init {
        show(annotationSectionTag().createElement(), { it.annotationHeading }, { it.annotationHint })
    }

    companion object : CustomElementReference<AnnotationsPage>(AnnotationsPage::class.js) {
        val NAME: TagName<AnnotationsPage> = TagName("reflect-annotations-page")

        fun define() {
            defineIfMissing(NAME, this)
        }
    }
}

/** Creates a defined `<reflect-annotations-page>`. */
internal fun newAnnotationsPage(): AnnotationsPage {
    AnnotationsPage.define()
    return AnnotationsPage.NAME()
}

/** Enums: entries, ordinals and the members the compiler keeps. */
internal class EnumsPage : SectionPage() {
    init {
        show(enumSectionTag().createElement(), { it.enumHeading }, { it.enumHint })
    }

    companion object : CustomElementReference<EnumsPage>(EnumsPage::class.js) {
        val NAME: TagName<EnumsPage> = TagName("reflect-enums-page")

        fun define() {
            defineIfMissing(NAME, this)
        }
    }
}

/** Creates a defined `<reflect-enums-page>`. */
internal fun newEnumsPage(): EnumsPage {
    EnumsPage.define()
    return EnumsPage.NAME()
}

/** Nested classes: the `Outer.Inner` names and their qualified form. */
internal class NestedPage : SectionPage() {
    init {
        show(nestedSectionTag().createElement(), { it.nestedHeading }, { it.nestedHint })
    }

    companion object : CustomElementReference<NestedPage>(NestedPage::class.js) {
        val NAME: TagName<NestedPage> = TagName("reflect-nested-page")

        fun define() {
            defineIfMissing(NAME, this)
        }
    }
}

/** Creates a defined `<reflect-nested-page>`. */
internal fun newNestedPage(): NestedPage {
    NestedPage.define()
    return NestedPage.NAME()
}

/** Limitations: what the wrapper cannot do on the JS/Native targets. */
internal class LimitsPage : SectionPage() {
    init {
        show(limitationsSectionTag().createElement(), { it.limitationsHeading }, { "" })
    }

    companion object : CustomElementReference<LimitsPage>(LimitsPage::class.js) {
        val NAME: TagName<LimitsPage> = TagName("reflect-limits-page")

        fun define() {
            defineIfMissing(NAME, this)
        }
    }
}

/** Creates a defined `<reflect-limits-page>`. */
internal fun newLimitsPage(): LimitsPage {
    LimitsPage.define()
    return LimitsPage.NAME()
}
