package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflectable

/** Opts in through the marker interface instead of the annotation, and is an `object`. */
public object Toolbox : KmpReflectable {
    public const val VERSION: String = "1.0"

    public fun hello(): String = "hello from the marker interface"
}
