package io.github.shijianjs.kmp.reflect

import kotlin.reflect.KClass

/**
 * The registry of every reflected class, and the entry point for lookups and scans.
 *
 * Metadata is generated per class by the KSP processor; a generated module object (see
 * [KmpReflectModule]) collects those classes so they can be registered in one call:
 *
 * ```kotlin
 * KmpReflection.register(Reflection)
 *
 * KmpClasses.of<Person>().function("greet")?.invoke(person, "hi")
 * KmpClasses.annotatedWith<Record>()
 * ```
 *
 * Scanning only ever sees classes whose module was registered — which is exactly the classes the
 * processor was told to reflect. There is no classpath walking at runtime, and nothing is loaded.
 */
public object KmpClasses {
    private val byName: MutableMap<String, KmpClass> = mutableMapOf()
    private val bySimpleName: MutableMap<String, MutableList<KmpClass>> = mutableMapOf()

    /** Registers [classes], replacing earlier entries with the same qualified name. */
    public fun register(classes: List<KmpClass>) {
        classes.forEach(::register)
    }

    /** Registers every given class, replacing earlier entries with the same qualified name. */
    public fun register(vararg classes: KmpClass) {
        classes.forEach(::register)
    }

    /** Registers one class, replacing an earlier entry with the same qualified name. */
    public fun register(kmpClass: KmpClass) {
        val previous = byName.put(kmpClass.qualifiedName, kmpClass)
        if (previous != null) {
            bySimpleName[previous.simpleName]?.remove(previous)
        }
        bySimpleName.getOrPut(kmpClass.simpleName) { mutableListOf() }.add(kmpClass)
    }

    /** Forgets every registered class. Intended for tests. */
    public fun clear() {
        byName.clear()
        bySimpleName.clear()
    }

    /** How many classes are registered. */
    public val size: Int
        get() = byName.size

    /** Every registered class, sorted by qualified name. */
    public fun all(): List<KmpClass> = byName.values.sortedBy { it.qualifiedName }

    /** The class with the given qualified name, or `null` when it was not registered. */
    public fun byNameOrNull(qualifiedName: String): KmpClass? = byName[qualifiedName]

    /**
     * The class with the given qualified name.
     *
     * @throws KmpReflectException when no such class is registered.
     */
    public fun byName(qualifiedName: String): KmpClass = byNameOrNull(qualifiedName)
        ?: throw KmpReflectException(
            "No reflected class named '$qualifiedName'. Annotate the class with @KmpReflect (or make " +
                "it implement KmpReflectable) and register the module it belongs to.",
        )

    /**
     * The metadata for [type].
     *
     * @throws KmpReflectException when the class is not registered.
     */
    public fun of(type: KClass<*>): KmpClass {
        // Identity first: it works on every target, including the ones without runtime names.
        byName.values.firstOrNull { candidate -> candidate.runtimeKClass == type }?.let { return it }
        val qualifiedName = KmpPlatformQualifiedName(type)
        if (qualifiedName != null) {
            byNameOrNull(qualifiedName)?.let { return it }
            // A local or nested class may only be known by its simple name, which is still unique in
            // practice; anything ambiguous stays unresolved.
            bySimpleName[qualifiedName.substringAfterLast('.')].orEmpty().singleOrNull()?.let { return it }
        }
        throw KmpReflectException(
            "No reflected class for '${qualifiedName ?: type}'. Annotate the class with @KmpReflect " +
                "(or make it implement KmpReflectable) and register the module it belongs to.",
        )
    }

    /** The metadata for `T`. */
    public inline fun <reified T : Any> of(): KmpClass = of(T::class)

    /** Every registered class matching [predicate], sorted by qualified name. */
    public fun find(predicate: (KmpClass) -> Boolean): List<KmpClass> = all().filter(predicate)

    /** Every registered class carrying an annotation whose qualified name is [qualifiedName]. */
    public fun annotatedWith(qualifiedName: String): List<KmpClass> =
        find { it.annotations.any { annotation -> annotation.qualifiedName == qualifiedName } }

    /** Every registered class carrying an annotation of type [A]. */
    public inline fun <reified A : Annotation> annotatedWith(): List<KmpClass> =
        annotatedWithName(kmpReflectiveName(A::class))

    /** [annotatedWith] for call sites in public inline functions. */
    @PublishedApi
    internal fun annotatedWithName(qualifiedName: String?): List<KmpClass> =
        if (qualifiedName == null) emptyList() else annotatedWith(qualifiedName)

    /**
     * Every registered class whose supertype chain reaches [qualifiedName], this class included when
     * it matches itself.
     *
     * Supertypes that are not registered are matched by name only, so a reflected class can report
     * that it implements a dependency's interface without that interface being reflected.
     */
    public fun implementing(qualifiedName: String): List<KmpClass> =
        find { candidate -> candidate.reachesSupertype(qualifiedName, mutableSetOf()) }

    /** Every registered class whose supertype chain reaches [type]. */
    public fun implementing(type: KClass<*>): List<KmpClass> =
        implementing(KmpPlatformQualifiedName(type) ?: return emptyList())

    private fun KmpClass.reachesSupertype(target: String, visited: MutableSet<String>): Boolean {
        if (target.isEmpty()) return false
        if (qualifiedName == target) return true
        if (!visited.add(qualifiedName)) return false
        return supertypes.any { supertype ->
            if (supertype.qualifiedName == target) {
                true
            } else {
                val resolved = byNameOrNull(supertype.qualifiedName)
                resolved != null && resolved.reachesSupertype(target, visited)
            }
        }
    }
}
