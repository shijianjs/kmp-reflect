package demo.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.junit.UsePlaywright
import com.microsoft.playwright.options.WaitUntilState
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

/**
 * Smoke checks for the demo: the bundle loads, the shell lists every route, the metadata sections
 * render, the language switch rewrites the page in place, and nothing logs an error or fails a request.
 *
 * The page and its sections live in shadow trees, which Playwright's CSS engine pierces; what it does
 * not do is find them across routes — each route has to be opened first, because the shell mounts one
 * page at a time.
 */
@UsePlaywright(DemoOptions::class)
class DemoPageTest {
    @Test
    fun `the shell renders the navbar, the logo and every route`(page: Page) {
        openPage(page)

        assertTrue(page.title().contains("kmp-reflect"), "title: ${page.title()}")
        assertTrue(page.locator("#demo img.layout__logo").isVisible(), "the shell serves the project logo")
        assertEquals("Reflected classes", page.locator("#demo reflect-classes-page h1").first().textContent())
        assertEquals(8, page.locator("#demo vaadin-side-nav-item[data-route]").count())
    }

    @Test
    fun `reflected classes are listed and selectable`(page: Page) {
        openPage(page)

        val personRow = page.locator("tr.row", Page.LocatorOptions().setHasText("demo.model.Person"))
        assertTrue(personRow.count() > 0, "Person is listed among the reflected classes")

        page.locator("tr.row", Page.LocatorOptions().setHasText("Person")).first().click()

        val members = page.locator(".detail .member").allTextContents()
        assertTrue(members.any { it.contains("fun greet(") }, "members: $members")
        assertTrue(members.any { it.contains("val greeting") }, "properties are listed too")
    }

    @Test
    fun `metadata drives construction, invocation and the proxy`(page: Page) {
        openPage(page)

        navigate(page, "invocation")
        val invocation = page.locator(".output").allTextContents().joinToString(" | ")
        assertTrue(invocation.contains("Hello, Ada!"), "invocation output: $invocation")

        navigate(page, "proxy")
        val proxy = page.locator(".output").allTextContents().joinToString(" | ")
        assertTrue(proxy.contains("proxy(greet) -> world"), "proxy output: $proxy")
    }

    @Test
    fun `annotation scan and inlined resources are shown`(page: Page) {
        openPage(page)

        navigate(page, "annotations")
        val annotations = page.locator(".output").allTextContents().joinToString(" | ")
        assertTrue(annotations.contains("name=person"), "annotation arguments: $annotations")

        navigate(page, "resources")
        val resources = page.locator(".output").allTextContents().joinToString(" | ")
        assertTrue(resources.contains("config.json"), "resource paths: $resources")
        // The SVG is rendered from the bytes the processor inlined, not from a file on disk.
        assertEquals(1, page.locator("img.thumb").count())
    }

    @Test
    fun `language switch rewrites the page in place`(page: Page) {
        openPage(page)

        switchLanguage(page, "zh-Hans")

        assertEquals("已反射的类", page.locator("#demo reflect-classes-page h1").first().textContent())
        // The class browser is data, not copy: the table is still there after the switch.
        assertTrue(page.locator("tr.row").count() > 0)
    }

    @Test
    fun `page logs no console errors or failed requests`(page: Page) {
        val consoleErrors = mutableListOf<String>()
        val failedRequests = mutableListOf<String>()
        page.onConsoleMessage { message ->
            if (message.type() == "error") consoleErrors += message.text()
        }
        page.onRequestFailed { request ->
            failedRequests += "${request.url()} ${request.failure()}"
        }

        openPage(page)
        navigate(page, "resources")

        assertTrue(consoleErrors.isEmpty(), "console errors: $consoleErrors")
        assertTrue(failedRequests.isEmpty(), "failed requests: $failedRequests")
    }

    private fun openPage(page: Page) {
        page.navigate("/", Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))
    }

    /** Opens a route through its sidebar item, which is also a check of the router. */
    private fun navigate(page: Page, route: String) {
        page.locator("#demo vaadin-side-nav-item[data-route='$route']").first().click()
        page.waitForSelector("#demo reflect-$route-page h1")
    }

    /** Opens the layout's language menu and picks [code]. */
    private fun switchLanguage(page: Page, code: String) {
        page.locator("#demo [data-menu='lang']").first().click()
        page.locator("#demo [data-action='lang:$code']").first().click()
    }
}
