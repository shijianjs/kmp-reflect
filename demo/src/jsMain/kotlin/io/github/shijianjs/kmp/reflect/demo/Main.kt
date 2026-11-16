package io.github.shijianjs.kmp.reflect.demo

import io.github.shijianjs.kmp.reflect.KmpReflection
import io.github.shijianjs.kmp.reflect.demo.page.DemoPage
import web.dom.ElementId
import web.dom.document

/**
 * Entry point: register the generated metadata, hand the host element to the page.
 *
 * Everything the page does lives in `page/`, split by the section it draws; this only wires the
 * generated module to the DOM.
 */
public fun main() {
    KmpReflection.register(DemoReflection)
    val host = document.getElementById(ElementId("demo")) ?: return
    DemoPage(host).mount()
}
