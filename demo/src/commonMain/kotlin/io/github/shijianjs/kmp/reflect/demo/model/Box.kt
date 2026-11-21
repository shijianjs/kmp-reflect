package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect

/** A type alias, so the metadata can be shown to record the expanded type. */
public typealias Label = String

/**
 * A generic class: its constructor, its properties and its members all mention `T`, which no generated
 * cast can name, so they are described but not invokable.
 */
@KmpReflect
public class Box<T : Any>(
    public val item: T,
    public val tags: List<T>,
    public val label: Label,
) {
    public fun swap(other: T): T = other

    /** A member whose signature is closed, so it stays invokable on a generic class. */
    public fun tagCount(): Int = tags.size
}
