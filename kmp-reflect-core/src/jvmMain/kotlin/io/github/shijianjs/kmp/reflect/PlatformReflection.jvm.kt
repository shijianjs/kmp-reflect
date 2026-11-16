package io.github.shijianjs.kmp.reflect

import kotlin.reflect.KClass

internal actual fun KmpPlatformQualifiedName(type: KClass<*>): String? = type.qualifiedName
