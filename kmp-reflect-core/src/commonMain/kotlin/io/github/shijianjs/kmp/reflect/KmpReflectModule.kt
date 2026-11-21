package io.github.shijianjs.kmp.reflect

import io.github.shijianjs.kmp.reflect.resource.KmpResource
import io.github.shijianjs.kmp.reflect.resource.KmpResources

/**
 * Thrown when reflection metadata is missing, a lookup fails, or an invocation is not supported.
 *
 * Every message names the declaration involved: the failure usually means a class was not annotated,
 * a module was not registered, or a signature is outside the generated subset.
 */
public class KmpReflectException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * The metadata one compiled module contributes: its reflected classes and its inlined resources.
 *
 * The KSP processor generates one implementation per module when the `kmp.reflect.registry` option
 * names a package and object (`com.example:Reflection`). Registration is explicit — call
 * [KmpReflection.register] once, before the first lookup:
 *
 * ```kotlin
 * KmpReflection.register(Reflection)
 * val metadata = KmpClasses.of<Person>()
 * ```
 */
public interface KmpReflectModule {
    /** A human-readable module name, used in diagnostics. */
    public val module: String

    /** Every reflected class of this module, in declaration-name order. */
    public val classes: List<KmpClass>

    /**
     * Qualified names of [classes], in declaration-name order.
     *
     * Registration reads this instead of [classes] so that a generated module never materialises its
     * metadata up front: [load] is called per name, on first lookup. Hand-built modules can keep
     * overriding [classes] only — the default derives the names from it.
     */
    public val classNames: List<String>
        get() = classes.map { it.qualifiedName }

    /**
     * The metadata of one reflected class, or `null` when this module does not reflect it.
     *
     * A generated module answers from the class's lazy property, so only that one gets built.
     */
    public fun load(qualifiedName: String): KmpClass? =
        classes.firstOrNull { it.qualifiedName == qualifiedName }

    /** Every inlined classpath resource of this module, in path order. */
    public val resources: List<KmpResource>
        get() = emptyList()
}

/** Coordinates registration of everything a module contributes. */
public object KmpReflection {
    /**
     * Registers [module]'s classes and resources.
     *
     * Registering the same module twice is harmless: existing entries are replaced, and lookups keep
     * working. Call it once per module at start-up; there is no automatic discovery (a KMP binary has
     * no portable class-initialisation hook, and `ServiceLoader` exists only on the JVM).
     *
     * Registration only records [KmpReflectModule.classNames]; the metadata of each class is built by
     * [KmpReflectModule.load] the first time something looks it up, so a module with many classes
     * does not pay for all of them at start-up.
     */
    public fun register(module: KmpReflectModule) {
        KmpClasses.register(module.classNames) { qualifiedName -> module.load(qualifiedName) }
        KmpResources.register(module.resources)
    }

    /** Removes every registered module's classes and resources. Intended for tests. */
    public fun clear() {
        KmpClasses.clear()
        KmpResources.clear()
    }
}
