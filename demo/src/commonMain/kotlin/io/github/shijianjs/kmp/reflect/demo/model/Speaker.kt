package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect

/** An interface with a default implementation: both members are declared, one is abstract. */
@KmpReflect
public interface Speaker {
    public fun speak(): String

    public fun twice(): String = speak() + speak()
}
