package io.github.shijianjs.kmp.reflect.demo

import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.KmpKind
import io.github.shijianjs.kmp.reflect.KmpModifier
import io.github.shijianjs.kmp.reflect.KmpReflectException
import io.github.shijianjs.kmp.reflect.KmpReflection
import io.github.shijianjs.kmp.reflect.demo.model.Calculator
import io.github.shijianjs.kmp.reflect.demo.model.Greeter
import io.github.shijianjs.kmp.reflect.demo.model.Library
import io.github.shijianjs.kmp.reflect.demo.model.Person
import io.github.shijianjs.kmp.reflect.demo.model.Shape
import io.github.shijianjs.kmp.reflect.demo.model.Toolbox
import io.github.shijianjs.kmp.reflect.resource.KmpResources
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Asserts the metadata the processor generated for the demo classes.
 *
 * `commonTest` rather than `jvmTest`: the same generated code is compiled into every target, so the
 * same assertions run on the JVM, in Node (JS), in Node (Wasm) and on the host's native target —
 * which is how each backend is shown to produce identical metadata.
 */
class ReflectionTest {
    private val model = "io.github.shijianjs.kmp.reflect.demo.model"

    @BeforeTest
    fun register() {
        KmpReflection.clear()
        KmpReflection.register(DemoReflection)
    }

    @Test
    fun `module registers every reflected class`() {
        // Person, its companion, Greeter, Shape, Toolbox, Calculator, Library and Library.Shelf
        assertEquals(8, KmpClasses.size)
        assertNotNull(KmpClasses.byNameOrNull("$model.Person"))
        assertNotNull(KmpClasses.byNameOrNull("$model.Person.Companion"))
        assertNotNull(KmpClasses.byNameOrNull("$model.Library.Shelf"))
    }

    @Test
    fun `class metadata mirrors the declaration`() {
        val person = KmpClasses.of(Person::class)
        assertEquals(KmpKind.CLASS, person.kind)
        assertEquals(FullyQualifiedPerson, person.qualifiedName)
        assertTrue(KmpModifier.DATA in person.modifiers)
        assertEquals(listOf("name", "age", "greeting"), person.properties.map { it.name })
        assertEquals(3, person.constructor(3)?.parameters?.size)
    }

    @Test
    fun `constructor and functions are invokable through metadata`() {
        val person = KmpClasses.of(Person::class)
        val instance = person.constructor(3)?.new("Ada", 36, "hidden") as? Person
        assertNotNull(instance)
        assertEquals("Ada", instance.name)

        assertEquals("Hello, Ada!", person.function("greet", 1)?.invoke(instance, "Hello"))
        assertEquals(37, person.function("birthday", 0)?.invoke(instance))
        assertEquals(37, person.property("age")?.get(instance))
        assertEquals("Hi, I'm Ada", person.property("greeting")?.get(instance))
    }

    @Test
    fun `ignored members are absent from the metadata`() {
        val person = KmpClasses.of(Person::class)
        assertNull(person.function("peekSecret", 0))
        // The private constructor parameter is still part of the signature, but not a property.
        assertEquals(listOf("name", "age", "greeting"), person.properties.map { it.name })
    }

    @Test
    fun `companion object is reflected and reachable`() {
        val person = KmpClasses.of(Person::class)
        val companion = assertNotNull(person.companion)
        assertEquals("$model.Person.Companion", companion.qualifiedName)
        assertNotNull(companion.function("create", 1))
        assertEquals("human", companion.property("SPECIES")?.get(companion.objectInstance))
    }

    @Test
    fun `proxy forwards calls to the handler`() {
        val greeterClass = KmpClasses.of(Greeter::class)
        assertTrue(greeterClass.isProxyable)
        var seen: String? = null
        val greeter = greeterClass.newProxy { _, function, args ->
            seen = "${function.name}/${args.first()}"
            "proxied: ${args.first()}"
        } as Greeter
        assertEquals("proxied: world", greeter.greet("world"))
        assertEquals("greet/world", seen)
    }

    @Test
    fun `annotation scan finds annotated classes and arguments`() {
        val annotated = KmpClasses.annotatedWith("$model.KmpRecord")
        assertEquals(
            setOf("$model.Person", "$model.Library.Shelf", "$model.Shape"),
            annotated.map { it.qualifiedName }.toSet(),
        )
        val person = KmpClasses.byName("$model.Person")
        val annotation = assertNotNull(person.annotation("$model.KmpRecord"))
        assertEquals("person", annotation.string("name"))
        assertEquals(3, annotation.get("priority"))
        assertTrue(annotation.get("tags") is List<*>)
    }

    @Test
    fun `enum entries keep ordinals and the instance`() {
        val shape = KmpClasses.of(Shape::class)
        assertTrue(shape.isEnum)
        assertEquals(listOf("CIRCLE", "TRIANGLE", "SQUARE"), shape.enumEntries.map { it.name })
        assertEquals(0, shape.enumEntries.first().ordinal)
        assertEquals(3, (shape.enumEntry("TRIANGLE")?.instance as Shape).sides)
        assertEquals(4, shape.property("sides")?.get(Shape.SQUARE))
    }

    @Test
    fun `marker interface opt in is reflected`() {
        val toolbox = KmpClasses.of(Toolbox::class)
        assertEquals(KmpKind.OBJECT, toolbox.kind)
        assertEquals("hello from the marker interface", toolbox.function("hello", 0)?.invoke(toolbox.objectInstance))
        assertEquals("1.0", toolbox.property("VERSION")?.get(toolbox.objectInstance))
        // By name rather than by reified type: Kotlin/JS and Kotlin/Wasm expose no runtime names, so
        // the `KClass` overload answers an empty list there. `docs/guide/platforms.md` says so.
        assertTrue(KmpClasses.implementing("$model.Toolbox").any { it.qualifiedName == "$model.Toolbox" })
    }

    @Test
    fun `unsupported members stay metadata only`() {
        val calculator = KmpClasses.of(Calculator::class)
        val identity = assertNotNull(calculator.function("identity", 1))
        assertFalse(identity.isInvokable)
        // `var lastResult` has a private setter, so metadata must not offer a write path.
        val lastResult = assertNotNull(calculator.property("lastResult"))
        assertFalse(lastResult.isWritable)
        assertEquals(3, calculator.function("add", 2)?.invoke(Calculator(), 1, 2))
    }

    @Test
    fun `suspend functions are invokable through the suspend entry point`() {
        val calculator = KmpClasses.of(Calculator::class)
        val slowAdd = assertNotNull(calculator.function("slowAdd", 2))
        assertTrue(slowAdd.isSuspend)
        assertTrue(slowAdd.isInvokable)
        assertTrue(slowAdd.isSuspendInvokable)
        // The generated lambda is `suspend`, so the call has to happen inside a coroutine. This
        // function does not suspend for real work, so the coroutine completes synchronously.
        assertEquals(7, runSuspend { slowAdd.invokeSuspend(Calculator(), 3, 4) })
        assertFailsWith<KmpReflectException> { assertNotNull(slowAdd).invoke(Calculator(), 3, 4) }
    }

    @Test
    fun `nested classes are linked both ways`() {
        val library = KmpClasses.of(Library::class)
        assertEquals(listOf("$model.Library.Shelf"), library.nestedClasses.map { it.qualifiedName })
        assertEquals("library", library.constructor(1)?.new("library")?.let { (it as Library).name })
    }

    @Test
    fun `inlined resources carry path mime type and bytes`() {
        assertEquals(3, KmpResources.size)
        val config = assertNotNull(KmpResources.byPath("config.json"))
        assertEquals("application/json", config.mimeType)
        assertTrue(config.text().contains("kmp-reflect demo"))
        val icon = assertNotNull(KmpResources.byPath("icons/star.svg"))
        assertEquals("image/svg+xml", icon.mimeType)
        assertTrue(icon.bytes.isNotEmpty())
        assertEquals(listOf("icons", "notes"), KmpResources.directories().filter { it.isNotEmpty() })
        assertEquals(1, KmpResources.allIn("notes").size)
    }

    private companion object {
        const val FullyQualifiedPerson = "io.github.shijianjs.kmp.reflect.demo.model.Person"

        /**
         * Runs a `suspend` lambda to completion without a coroutines library.
         *
         * `startCoroutine` is standard library API, so this works on every target the tests run on.
         * It only works for coroutines that complete without suspending — which is exactly what the
         * generated lambda does once the reflected function returns.
         */
        fun <T> runSuspend(block: suspend () -> T): T {
            var result: Result<T>? = null
            block.startCoroutine(
                Continuation(EmptyCoroutineContext) { outcome -> result = outcome },
            )
            return checkNotNull(result) { "the coroutine suspended; this helper needs one that completes" }
                .getOrThrow()
        }
    }
}
