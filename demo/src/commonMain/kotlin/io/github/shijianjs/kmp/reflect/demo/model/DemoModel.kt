package io.github.shijianjs.kmp.reflect.demo.model

/**
 * A demo annotation: it exists so the page and the tests can show annotation scanning.
 *
 * Retention is `BINARY` — the processor reads it from the sources, and nothing needs it at runtime.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class KmpRecord(
    val name: String,
    val tags: Array<String> = [],
    val priority: Int = 0,
)
