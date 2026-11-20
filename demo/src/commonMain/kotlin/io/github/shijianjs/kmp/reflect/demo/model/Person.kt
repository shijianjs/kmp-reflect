package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect
import io.github.shijianjs.kmp.reflect.KmpReflectIgnore

/** The main demo class: a data class with a companion, private state and an excluded member. */
@KmpRecord(name = "person", tags = ["sample", "user"], priority = 3)
@KmpReflect
public data class Person(
    val name: String,
    var age: Int = 0,
    private val secret: String = "hidden",
) {
    /** A derived property: only a getter exists, so metadata marks it read-only. */
    @KmpRecord(name = "greeting", priority = 1)
    public val greeting: String
        get() = "Hi, I'm $name"

    /** A function with a default argument; callers still have to pass every argument. */
    public fun greet(prefix: String = "Hello"): String = "$prefix, $name!"

    /** Mutates state, so a call is visible through the reflected property afterwards. */
    public fun birthday(): Int {
        age += 1
        return age
    }

    @KmpReflectIgnore
    public fun peekSecret(): String = secret

    public companion object {
        public const val SPECIES: String = "human"

        public fun create(name: String): Person = Person(name)
    }
}
