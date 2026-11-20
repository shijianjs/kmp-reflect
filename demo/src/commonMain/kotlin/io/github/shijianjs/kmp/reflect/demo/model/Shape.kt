package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect

/** An enum with a constructor property. */
@KmpRecord(name = "shape", tags = ["geometry"])
@KmpReflect
public enum class Shape(public val sides: Int) {
    CIRCLE(0),
    TRIANGLE(3),
    SQUARE(4),
}
