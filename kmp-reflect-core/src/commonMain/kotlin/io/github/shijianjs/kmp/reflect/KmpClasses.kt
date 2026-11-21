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
 * Registration is lazy: a module only records its class names, and a class's metadata is built the
 * first time a lookup touches it. Scans ([all], [find], [annotatedWith], [implementing]) are the only
 * operations that build everything, because they have to look at everything.
 *
 * Scanning only ever sees classes whose module was registered — which is exactly the classes the
 * processor was told to reflect. There is no classpath walking at runtime, and nothing is loaded.
 */
public object KmpClasses {
    /** Metadata that has been built so far, by qualified name. */
    private val byName: MutableMap<String, KmpClass> = mutableMapOf()
    private val bySimpleName: MutableMap<String, MutableList<KmpClass>> = mutableMapOf()

    /** Built metadata keyed by its runtime class, so `of(KClass)` does not have to scan. */
    private val byRuntime: MutableMap<KClass<*>, KmpClass> = mutableMapOf()

    /** Names registered but not built yet, with the callable that builds one of them. */
    private val loaders: MutableMap<String, () -> KmpClass?> = mutableMapOf()

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
        loaders.remove(kmpClass.qualifiedName)
        index(kmpClass)
    }

    /**
     * Records [classNames] without building anything; [load] runs on the first lookup of one name.
     *
     * This is how [KmpReflection.register] wires a module: the module decides whether a name is
     * backed by an eager list or by a lazy property, and the registry only pays for what is asked.
     */
    internal fun register(classNames: List<String>, load: (String) -> KmpClass?) {
        classNames.forEach { qualifiedName ->
            loaders[qualifiedName] = { load(qualifiedName) }
        }
    }

    /** Forgets every registered class. Intended for tests. */
    public fun clear() {
        byName.clear()
        bySimpleName.clear()
        byRuntime.clear()
        loaders.clear()
    }

    /** How many classes are registered, built or not. */
    public val size: Int
        get() = (byName.keys + loaders.keys).size

    /** Every registered class, sorted by qualified name. Builds metadata that was not needed yet. */
    public fun all(): List<KmpClass> {
        // Loading may re-enter, so drain by key instead of iterating the map.
        while (loaders.isNotEmpty()) {
            resolve(loaders.keys.first())
        }
        return byName.values.sortedBy { it.qualifiedName }
    }

    /** The class with the given qualified name, or `null` when it was not registered. */
    public fun byNameOrNull(qualifiedName: String): KmpClass? = resolve(qualifiedName)

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
     * Looks the runtime class up in the index first, then by qualified name, then by a unique simple
     * name. Only when a target reports no usable name does it fall back to comparing [KmpClass.kclass]
     * identity, which has to build every class to compare.
     *
     * @throws KmpReflectException when the class is not registered.
     */
    public fun of(type: KClass<*>): KmpClass {
        byRuntime[type]?.let { return it }
        val qualifiedName = KmpPlatformQualifiedName(type)
        if (qualifiedName != null) {
            resolve(qualifiedName)?.let { return it }
            // A local or nested class may only be known by its simple name, which is still unique in
            // practice; anything ambiguous stays unresolved.
            bySimpleName[qualifiedName.substringAfterLast('.')].orEmpty().singleOrNull()?.let { return it }
        }
        all().firstOrNull { candidate -> candidate.kclass == type }?.let { return it }
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

    /**
     * [annotatedWith] for call sites in public inline functions.
     *
     * Compares the qualified name first and the annotation's simple name second: Kotlin/Native hands
     * out simple names only, and Kotlin/JS and Wasm hand out none at all.
     */
    @PublishedApi
    internal fun annotatedWithName(name: String?): List<KmpClass> =
        if (name == null) {
            emptyList()
        } else {
            find { candidate ->
                candidate.annotations.any { annotation ->
                    annotation.qualifiedName == name || annotation.type.simpleName == name
                }
            }
        }

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

    /** Built metadata for [qualifiedName], building it through its loader on first use. */
    private fun resolve(qualifiedName: String): KmpClass? {
        byName[qualifiedName]?.let { return it }
        val load = loaders.remove(qualifiedName) ?: return null
        val loaded = load() ?: return null
        index(loaded)
        return loaded
    }

    private fun index(kmpClass: KmpClass) {
        val previous = byName.put(kmpClass.qualifiedName, kmpClass)
        if (previous != null) {
            bySimpleName[previous.simpleName]?.let { bucket ->
                bucket.remove(previous)
                if (bucket.isEmpty()) bySimpleName.remove(previous.simpleName)
            }
            previous.kclass?.let { byRuntime.remove(it) }
        }
        bySimpleName.getOrPut(kmpClass.simpleName) { mutableListOf() }.add(kmpClass)
        kmpClass.kclass?.let { byRuntime[it] = kmpClass }
    }

    private fun KmpClass.reachesSupertype(target: String, visited: MutableSet<String>): Boolean {
        if (target.isEmpty()) return false
        // Native only supplies simple names, so `implementing(SomeInterface::class)` is answered by
        // either form here; a fully qualified target stays exact.
        if (qualifiedName == target || simpleName == target) return true
        if (!visited.add(qualifiedName)) return false
        return supertypes.any { supertype ->
            if (supertype.qualifiedName == target || supertype.simpleName == target) {
                true
            } else {
                val resolved = byNameOrNull(supertype.qualifiedName)
                resolved != null && resolved.reachesSupertype(target, visited)
            }
        }
    }
}
