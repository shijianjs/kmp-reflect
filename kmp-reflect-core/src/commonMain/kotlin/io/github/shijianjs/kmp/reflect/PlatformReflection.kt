package io.github.shijianjs.kmp.reflect

import kotlin.reflect.KClass

/**
 * The compiler-visible name of [type], or `null` where the platform keeps none.
 *
 * This is the one place the library touches `KClass` naming. Kotlin/JS does not expose qualified
 * names at runtime, so `null` there is expected: metadata still carries every name, only the
 * reverse lookup from a reified type degrades.
 */
internal expect fun KmpPlatformQualifiedName(type: KClass<*>): String?

/** [KmpPlatformQualifiedName] for call sites in public inline functions. */
@PublishedApi
internal fun kmpReflectiveName(type: KClass<*>): String? = KmpPlatformQualifiedName(type)

/** Whether [type] is the class this reference describes, by identity or — where available — by name. */
@PublishedApi
internal fun KmpTypeRef.matches(type: KClass<*>, name: String?): Boolean =
    runtimeKClass == type || (name != null && qualifiedName == name)
