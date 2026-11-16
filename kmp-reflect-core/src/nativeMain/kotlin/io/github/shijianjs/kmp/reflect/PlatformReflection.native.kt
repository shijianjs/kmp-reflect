package io.github.shijianjs.kmp.reflect

import kotlin.reflect.KClass

/**
 * Kotlin/Native exposes simple names but no qualified ones, so the metadata is the only source of a
 * fully qualified name. Callers that need exactness use [KmpClasses.byName]; reified lookups still
 * resolve, because identity comparison comes first and the simple name is unique among the classes a
 * module registers.
 */
internal actual fun KmpPlatformQualifiedName(type: KClass<*>): String? = type.simpleName
