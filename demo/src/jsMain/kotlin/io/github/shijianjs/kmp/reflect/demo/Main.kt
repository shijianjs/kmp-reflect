package io.github.shijianjs.kmp.reflect.demo

import io.github.shijianjs.kmp.reflect.KmpReflection
import io.github.shijianjs.kmp.reflect.demo.page.demoPageTag
import io.github.shijianjs.kmp.reflect.demo.page.header.headerBarTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sectionShellTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.annotationSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.classBrowserTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.enumSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.invocationSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.limitationsSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.nestedSectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.proxySectionTag
import io.github.shijianjs.kmp.reflect.demo.page.section.sections.resourcesSectionTag

/**
 * Entry point: register the generated metadata, then define the page's custom elements.
 *
 * The order matters. `<kmp-demo-page>` is already in the markup, so defining its tag upgrades it
 * immediately and its constructor creates the children — the section tags must be defined first,
 * otherwise those children would be unknown elements that upgrade later, in an order nobody controls.
 * `<kmp-section>` comes before the sections for the same reason one level down: each section's
 * constructor creates its shell. Metadata comes first of all: every section reads it while constructing.
 */
public fun main() {
    KmpReflection.register(DemoReflection)

    sectionShellTag()
    headerBarTag()
    classBrowserTag()
    invocationSectionTag()
    proxySectionTag()
    resourcesSectionTag()
    annotationSectionTag()
    enumSectionTag()
    nestedSectionTag()
    limitationsSectionTag()
    demoPageTag()
}
