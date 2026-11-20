package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect

/** A reflected base class, so `Calculator` shows a superclass linked to its own metadata. */
@KmpReflect
public open class Machine {
    public open fun power(): Int = 1
}
