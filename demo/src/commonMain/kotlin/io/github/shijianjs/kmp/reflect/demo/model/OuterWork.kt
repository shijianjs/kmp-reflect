package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect

/** An outer class with a reflected `inner` class, whose constructor needs an outer instance. */
@KmpReflect
public class OuterWork {
    public fun work(): String = "work"

    @KmpReflect
    public inner class Inner {
        public fun ping(): String = "pong"
    }
}
