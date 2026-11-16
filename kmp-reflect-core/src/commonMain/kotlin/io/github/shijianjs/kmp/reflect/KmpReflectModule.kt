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
     */
    public fun register(module: KmpReflectModule) {
        KmpClasses.register(module.classes)
        KmpResources.register(module.resources)
    }

    /** Removes every registered module's classes and resources. Intended for tests. */
    public fun clear() {
        KmpClasses.clear()
        KmpResources.clear()
    }
}
