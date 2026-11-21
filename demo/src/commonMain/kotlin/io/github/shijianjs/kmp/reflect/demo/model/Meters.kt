package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect
import kotlin.jvm.JvmInline

/** A `value class`: single-field, with a member that stays callable through metadata. */
@JvmInline
@KmpReflect
public value class Meters(public val value: Int) {
    public fun doubled(): Int = value * 2
}
