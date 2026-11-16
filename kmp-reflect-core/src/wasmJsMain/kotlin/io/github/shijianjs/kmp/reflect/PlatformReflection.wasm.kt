package io.github.shijianjs.kmp.reflect

import kotlin.reflect.KClass

/**
 * Kotlin/Wasm keeps the same limited reflection as Kotlin/JS: class identity is available, names are
 * not. Metadata carries every name, so only the reverse lookup from a reified type degrades.
 */
internal actual fun KmpPlatformQualifiedName(type: KClass<*>): String? = null
