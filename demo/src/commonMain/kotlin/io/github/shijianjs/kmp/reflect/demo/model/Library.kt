package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect

/** A class with a nested reflected class, so the page can show nesting. */
@KmpReflect
public class Library(public val name: String) {
    @KmpRecord(name = "shelf", tags = ["nested"])
    @KmpReflect
    public class Shelf(public val books: Int)

    public fun open(): String = "$name is open"
}
