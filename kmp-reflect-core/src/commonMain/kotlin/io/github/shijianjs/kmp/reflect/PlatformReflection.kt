package io.github.shijianjs.kmp.reflect

import kotlin.reflect.KClass

/**
 * The compiler-visible name of [type], or `null` where the platform keeps none.
 *
 * This is the one place the library touches `KClass` naming. Kotlin/JS and Kotlin/Wasm expose no
 * names at all, so `null` there is expected; Kotlin/Native exposes a simple name, the JVM a qualified
 * one. Metadata still carries every qualified name, so only the reverse lookup from a reified type
 * changes shape per platform.
 */
internal expect fun KmpPlatformQualifiedName(type: KClass<*>): String?

/** [KmpPlatformQualifiedName] for call sites in public inline functions. */
@PublishedApi
internal fun kmpReflectiveName(type: KClass<*>): String? = KmpPlatformQualifiedName(type)

/**
 * Whether [type] is the class this reference describes: by identity first, which works on every
 * platform, then by the name the platform was able to supply — qualified on the JVM, simple on
 * Native, absent on JS and Wasm.
 */
@PublishedApi
internal fun KmpTypeRef.matches(type: KClass<*>, name: String?): Boolean =
    runtimeKClass == type || (name != null && (qualifiedName == name || simpleName == name))
