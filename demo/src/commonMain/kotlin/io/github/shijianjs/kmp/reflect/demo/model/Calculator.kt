package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect

/** Exercises members the processor records but cannot invoke. */
@KmpReflect
public open class Calculator : Machine() {
    public var lastResult: Int = 0
        private set

    public open fun add(left: Int, right: Int): Int = (left + right).also { lastResult = it }

    public operator fun plus(other: Calculator): Calculator = Calculator()

    public suspend fun slowAdd(left: Int, right: Int): Int = left + right

    public fun <T> identity(value: T): T = value
}
