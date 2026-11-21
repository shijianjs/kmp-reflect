package io.github.shijianjs.kmp.reflect.demo.model

import io.github.shijianjs.kmp.reflect.KmpReflect
import kotlin.reflect.KClass

/** A nested annotation used as an argument value. */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class KmpNested(val flag: Boolean = true)

/** Every argument kind the processor can evaluate: class, enum entry, nested annotation, array. */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class KmpSpec(
    val target: KClass<*>,
    val shape: Shape = Shape.CIRCLE,
    val nested: KmpNested = KmpNested(),
    val labels: Array<String> = ["a", "b"],
)

/** A class carrying [KmpSpec], so the tests can read back each argument kind. */
@KmpReflect
@KmpSpec(target = Person::class, labels = ["x", "y"])
public class Specimen
