package io.github.shijianjs.kmp.reflect

import kotlin.reflect.KClass

/**
 * Kotlin/JS keeps no qualified names at runtime; generated metadata is the name source there, so
 * only identity comparisons can resolve a reified type.
 */
internal actual fun KmpPlatformQualifiedName(type: KClass<*>): String? = null
