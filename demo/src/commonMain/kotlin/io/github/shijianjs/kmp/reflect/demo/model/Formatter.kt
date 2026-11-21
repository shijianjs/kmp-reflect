package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect

/**
 * A default value in the middle of the parameter list: `mode` may be omitted in Kotlin (by naming
 * `unit`), but positional metadata cannot skip it, so every argument stays required.
 */
@KmpReflect
public class Formatter {
    public fun format(value: Int, mode: String = "-", unit: String): String = "$value$mode$unit"
}
