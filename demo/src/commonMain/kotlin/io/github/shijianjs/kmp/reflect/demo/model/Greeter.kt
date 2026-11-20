package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect

/** A `fun interface`: reflected metadata plus a generated dynamic proxy. */
@KmpReflect
public fun interface Greeter {
    public fun greet(name: String): String
}
