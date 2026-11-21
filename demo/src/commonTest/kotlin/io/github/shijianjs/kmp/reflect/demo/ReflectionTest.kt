package io.github.shijianjs.kmp.reflect.demo

import io.github.shijianjs.kmp.reflect.KmpClasses
import io.github.shijianjs.kmp.reflect.KmpKind
import io.github.shijianjs.kmp.reflect.KmpModifier
import io.github.shijianjs.kmp.reflect.KmpReflectException
import io.github.shijianjs.kmp.reflect.KmpReflection
import io.github.shijianjs.kmp.reflect.KmpVisibility
import io.github.shijianjs.kmp.reflect.demo.model.Box
import io.github.shijianjs.kmp.reflect.demo.model.Calculator
import io.github.shijianjs.kmp.reflect.demo.model.Formatter
import io.github.shijianjs.kmp.reflect.demo.model.Greeter
import io.github.shijianjs.kmp.reflect.demo.model.Library
import io.github.shijianjs.kmp.reflect.demo.model.Machine
import io.github.shijianjs.kmp.reflect.demo.model.Meters
import io.github.shijianjs.kmp.reflect.demo.model.OuterWork
import io.github.shijianjs.kmp.reflect.demo.model.Person
import io.github.shijianjs.kmp.reflect.demo.model.Scoped
import io.github.shijianjs.kmp.reflect.demo.model.Shape
import io.github.shijianjs.kmp.reflect.demo.model.Speaker
import io.github.shijianjs.kmp.reflect.demo.model.Specimen
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
        // Person, its companion, Greeter, Shape, Toolbox, Calculator, Machine, Library, Library.Shelf,
        // Scoped, Box, OuterWork, OuterWork.Inner, Meters, Speaker, Formatter and Specimen
        assertEquals(17, KmpClasses.size)
        assertNotNull(KmpClasses.byNameOrNull("$model.Person"))
        assertNotNull(KmpClasses.byNameOrNull("$model.Person.Companion"))
        assertNotNull(KmpClasses.byNameOrNull("$model.Library.Shelf"))
        assertNotNull(KmpClasses.byNameOrNull("$model.OuterWork.Inner"))
        assertNotNull(KmpClasses.byNameOrNull("$model.Meters"))
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
    fun `reflected supertypes are linked to their metadata`() {
        val calculator = KmpClasses.of(Calculator::class)
        assertEquals(listOf("$model.Machine"), calculator.reflectedSupertypes.map { it.qualifiedName })
        // `Machine` itself has no reflected supertype, so the chain ends there.
        assertEquals(listOf("$model.Machine"), calculator.allReflectedSupertypes.map { it.qualifiedName })
        assertEquals(1, calculator.reflectedSupertypes.single().function("power", 0)?.invoke(Machine()))
        // `Person` declares no supertype worth linking (data classes only add `Any`).
        assertTrue(KmpClasses.of(Person::class).reflectedSupertypes.isEmpty())
    }

    @Test
    fun `subtype questions answer by name and by reflected chain`() {
        val calculator = KmpClasses.of(Calculator::class)
        assertTrue(calculator.isSubclassOf("$model.Machine"))
        assertTrue(calculator.isSubclassOf("Machine"))
        // The declaration itself does not count, and neither does an unrelated name.
        assertFalse(calculator.isSubclassOf("$model.Calculator"))
        assertFalse(calculator.isSubclassOf("$model.Person"))
        // An interface with no declared supertype reaches nothing.
        assertFalse(KmpClasses.of(Greeter::class).isSubclassOf("$model.Speaker"))
    }

    @Test
    fun `all functions merge the reflected hierarchy once`() {
        // `Calculator` declares four functions, and `power` comes from `Machine`: five, once each.
        val calculator = KmpClasses.of(Calculator::class)
        val declared = calculator.functions.map { it.name }
        val inherited = KmpClasses.of(Machine::class).functions.map { it.name }

        assertEquals((declared + inherited).toSet(), calculator.allFunctions().map { it.name }.toSet())
        assertEquals(declared.size + inherited.size, calculator.allFunctions().size)
        // The inherited one is reachable through the merged list, which is the point of the merge.
        assertEquals(1, calculator.allFunctions().filter { it.name == "power" }.size)
    }

    @Test
    fun `nested classes are linked both ways`() {
        val library = KmpClasses.of(Library::class)
        assertEquals(listOf("$model.Library.Shelf"), library.nestedClasses.map { it.qualifiedName })
        assertEquals("library", library.constructor(1)?.new("library")?.let { (it as Library).name })
    }

    @Test
    fun `protected and private members never reach the metadata`() {
        val scoped = KmpClasses.of(Scoped::class)
        // A generated file is outside the class: a protected or private call there would not compile,
        // so such members are dropped instead of described.
        assertNull(scoped.function("hidden", 0))
        assertNull(scoped.function("secret", 0))
        // The protected secondary constructor is gone, the public primary one stays.
        assertEquals(1, scoped.constructors.size)
        assertEquals("x", (scoped.constructor(1)?.new("x") as Scoped).label)
        // A public property with a protected setter is readable but not writable.
        val counter = assertNotNull(scoped.property("counter"))
        assertTrue(counter.isReadable)
        assertFalse(counter.isWritable)
    }

    @Test
    fun `internal members keep their visibility`() {
        val scoped = KmpClasses.of(Scoped::class)
        val internalThing = assertNotNull(scoped.function("internalThing", 0))
        assertEquals(KmpVisibility.INTERNAL, internalThing.visibility)
        assertEquals(KmpVisibility.PUBLIC, assertNotNull(scoped.function("shown", 0)).visibility)
        assertEquals(KmpVisibility.PUBLIC, assertNotNull(scoped.constructor(1)).visibility)
        assertEquals("internal", internalThing.invoke(Scoped("x")))
    }

    @Test
    fun `vararg parameters take one array argument`() {
        val join = assertNotNull(KmpClasses.of(Scoped::class).function("join", 1))
        assertTrue(join.parameters.single().isVararg)
        assertEquals("a-b", join.invoke(Scoped("x"), arrayOf("a", "b")))
    }

    @Test
    fun `a generic class is described and its closed members stay invokable`() {
        val box = KmpClasses.of(Box::class)
        assertEquals(listOf("T"), box.typeParameters.map { it.name })
        // A cast cannot name `T`, so the members that mention it are described only.
        assertFalse(assertNotNull(box.constructor(3)).isInvokable)
        assertFalse(assertNotNull(box.function("swap", 1)).isInvokable)
        assertFalse(assertNotNull(box.property("item")).isReadable)
        assertFalse(assertNotNull(box.property("tags")).isReadable)
        // Members with a closed signature are still callable: the receiver cast is `Box<*>`.
        val instance = Box("x", listOf("a", "b"), "label")
        assertEquals(2, box.function("tagCount", 0)?.invoke(instance))
        // The type alias is recorded expanded, not by its own name.
        assertEquals("kotlin.String", assertNotNull(box.property("label")).type.qualifiedName)
        assertEquals("label", box.property("label")?.get(instance))
    }

    @Test
    fun `a value class can be rebuilt and called through metadata`() {
        val meters = KmpClasses.of(Meters::class)
        assertTrue(KmpModifier.VALUE in meters.modifiers)
        val instance = assertNotNull(meters.constructor(1)?.new(3)) as Meters
        assertEquals(3, instance.value)
        assertEquals(6, meters.function("doubled", 0)?.invoke(instance))
    }

    @Test
    fun `an inner class needs an outer instance`() {
        val inner = KmpClasses.of(OuterWork.Inner::class)
        assertTrue(KmpModifier.INNER in inner.modifiers)
        assertFalse(assertNotNull(inner.constructor(0)).isInvokable)
        // The call itself still works on an instance the caller obtained the Kotlin way.
        val outer = OuterWork()
        assertEquals("pong", inner.function("ping", 0)?.invoke(outer.Inner()))
    }

    @Test
    fun `an interface default member is callable and the interface is proxyable`() {
        val speaker = KmpClasses.of(Speaker::class)
        assertTrue(speaker.isInterface)
        assertTrue(speaker.isProxyable)
        val implementation = object : Speaker {
            override fun speak(): String = "hi"
        }
        assertEquals("hi", speaker.function("speak", 0)?.invoke(implementation))
        assertEquals("hihi", speaker.function("twice", 0)?.invoke(implementation))
    }

    @Test
    fun `annotation arguments cover class enum nested and array values`() {
        val specimen = KmpClasses.of(Specimen::class)
        val spec = assertNotNull(specimen.annotation("$model.KmpSpec"))
        assertEquals(Person::class, spec.kclass("target"))
        assertEquals(Shape.CIRCLE, spec.get("shape"))
        assertTrue(spec.get("labels") is List<*>)
        assertEquals(listOf("x", "y"), spec.get("labels"))
        val nested = assertNotNull(spec.annotation("nested"))
        assertEquals("$model.KmpNested", nested.qualifiedName)
        assertEquals(true, nested.boolean("flag"))
    }

    @Test
    fun `trailing defaults may be omitted from construction and calls`() {
        val person = KmpClasses.of(Person::class)
        val constructor = assertNotNull(person.constructor(3))
        // `age` and `secret` declare defaults, so they can be left out.
        val ada = constructor.new("Ada") as Person
        assertEquals(0, ada.age)
        // The same rule applies to member calls and to the compiler-generated `copy`.
        assertEquals("Hello, Ada!", person.function("greet", 1)?.invoke(ada))
        val grace = assertNotNull(person.function("copy", 3)).invoke(ada, "Grace") as Person
        assertEquals("Grace", grace.name)
        assertEquals(0, grace.age)
        // Below the allowed range the call is still rejected.
        assertFailsWith<KmpReflectException> { constructor.new() }
    }

    @Test
    fun `a default in the middle of the list keeps every argument required`() {
        val format = assertNotNull(KmpClasses.of(Formatter::class).function("format", 3))
        // `mode` has a default, but the last parameter does not, so no tail can be omitted.
        assertFailsWith<KmpReflectException> { format.invoke(Formatter(), 1, "x") }
        assertEquals("1-!", format.invoke(Formatter(), 1, "-", "!"))
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
