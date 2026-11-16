package io.github.shijianjs.kmp.reflect.resource

import io.github.shijianjs.kmp.reflect.KmpReflectException

/**
 * The registry of inlined classpath resources.
 *
 * Resources arrive through a module's [io.github.shijianjs.kmp.reflect.KmpReflectModule], which
 * [io.github.shijianjs.kmp.reflect.KmpReflection.register] forwards here. Lookups are by path only —
 * there is no directory listing at runtime, but every entry knows its own [KmpResource.directory], so
 * [`allIn`][allIn] can group them.
 */
public object KmpResources {
    private val byPath: MutableMap<String, KmpResource> = mutableMapOf()

    /** Registers [resources], replacing earlier entries with the same path. */
    public fun register(resources: List<KmpResource>) {
        resources.forEach(::register)
    }

    /** Registers every given resource, replacing earlier entries with the same path. */
    public fun register(vararg resources: KmpResource) {
        resources.forEach(::register)
    }

    /** Registers one resource, replacing an earlier entry with the same path. */
    public fun register(resource: KmpResource) {
        byPath[resource.path] = resource
    }

    /** Forgets every registered resource. Intended for tests. */
    public fun clear() {
        byPath.clear()
    }

    /** How many resources are registered. */
    public val size: Int
        get() = byPath.size

    /** Every registered resource, sorted by path. */
    public fun all(): List<KmpResource> = byPath.values.sortedBy { it.path }

    /** The resource with the given path, or `null` when it was not registered. */
    public fun byPathOrNull(path: String): KmpResource? = byPath[path]

    /**
     * The resource with the given path.
     *
     * @throws KmpReflectException when no such resource was inlined.
     */
    public fun byPath(path: String): KmpResource = byPathOrNull(path)
        ?: throw KmpReflectException(
            "No inlined resource at '$path'. Check the 'kmp.reflect.resources' KSP option and the " +
                "path relative to that directory.",
        )

    /** Every resource whose [KmpResource.directory] is exactly [directory]. */
    public fun allIn(directory: String): List<KmpResource> = all().filter { it.directory == directory }

    /** The text content of the resource at [path], decoded as UTF-8. */
    public fun text(path: String): String = byPath(path).text()

    /** Every distinct directory that contains at least one resource, sorted. */
    public fun directories(): List<String> = all().map { it.directory }.distinct().sorted()
}
