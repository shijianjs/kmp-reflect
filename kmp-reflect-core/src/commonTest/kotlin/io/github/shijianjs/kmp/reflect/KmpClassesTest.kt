package io.github.shijianjs.kmp.reflect

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for the metadata model and the registry, built by hand instead of by KSP.
 *
 * The generated-code path is covered end to end by `:demo:reflectionTest`; what is tested here is the
 * behaviour of the model itself, which has to hold on every platform — these run on both the JVM and
 * Node.
 */
class KmpClassesTest {
    @AfterTest
    fun clearRegistry() {
        KmpReflection.clear()
    }

    private fun person() = KmpClass(
        qualifiedName = "com.example.Person",
        kind = KmpKind.CLASS,
        modifiers = setOf(KmpModifier.DATA),
        annotations = listOf(KmpAnnotation(KmpTypeRef("com.example.Record"), mapOf("name" to "person"))),
        functions = listOf(
            KmpFunction(
                name = "greet",
                returnType = KmpTypeRef("kotlin.String"),
                parameters = listOf(KmpParameter("prefix", KmpTypeRef("kotlin.String"), 0)),
                invoker = { receiver, arguments -> "${arguments[0]}, ${(receiver as String).length}" },
            ),
            KmpFunction(
                name = "greet",
                returnType = KmpTypeRef("kotlin.String"),
                parameters = listOf(
                    KmpParameter("prefix", KmpTypeRef("kotlin.String"), 0),
                    KmpParameter("times", KmpTypeRef("kotlin.Int"), 1),
                ),
                invoker = { _, arguments -> "overload ${arguments[1]}" },
            ),
            KmpFunction(
                name = "slow",
                returnType = KmpTypeRef("kotlin.String"),
                modifiers = setOf(KmpModifier.SUSPEND),
                isInvokable = false,
            ),
        ),
    )

    private fun module(vararg classes: KmpClass): KmpReflectModule = object : KmpReflectModule {
        override val module: String = "test"
        override val classes: List<KmpClass> = classes.toList()
    }

    @Test
    fun `registration makes classes visible by name`() {
        KmpReflection.register(module(person()))

        assertEquals("com.example.Person", KmpClasses.byNameOrNull("com.example.Person")?.qualifiedName)
        assertEquals(1, KmpClasses.size)
        assertNull(KmpClasses.byNameOrNull("com.example.Missing"))
    }

    @Test
    fun `clear removes every class`() {
        KmpReflection.register(module(person()))
        KmpReflection.clear()

        assertTrue(KmpClasses.all().isEmpty())
        assertNull(KmpClasses.byNameOrNull("com.example.Person"))
    }

    @Test
    fun `a class registered twice keeps the last metadata`() {
        KmpReflection.register(module(person()))
        KmpReflection.register(module(KmpClass("com.example.Person", modifiers = setOf(KmpModifier.OPEN))))

        assertEquals(1, KmpClasses.size)
        assertTrue(KmpModifier.OPEN in KmpClasses.byName("com.example.Person").modifiers)
    }

    @Test
    fun `functions are found by name and arity`() {
        val person = person()

        assertEquals(1, person.function("greet", 1)?.parameters?.size)
        assertEquals(2, person.function("greet", 2)?.parameters?.size)
        assertNull(person.function("greet", 3))
        assertEquals(2, person.functionsNamed("greet").size)
    }

    @Test
    fun `invocation passes arguments and returns the result`() {
        val greet = person().function("greet", 1)!!

        assertEquals("Hi, 3", greet.invoke("Ada", "Hi"))
        assertEquals("Hi, 3", greet.invokeAs<String>("Ada", "Hi"))
    }

    @Test
    fun `invoking a non invokable function fails with a reason`() {
        val slow = person().function("slow", 0)!!

        assertFalse(slow.isInvokable)
        assertTrue(slow.isSuspend)
        val error = assertFailsWith<KmpReflectException> { slow.invoke("Ada") }
        assertTrue(error.message!!.contains("slow"))
    }

    @Test
    fun `invoking with the wrong argument count fails`() {
        val greet = person().function("greet", 1)!!

        assertFailsWith<KmpReflectException> { greet.invoke("Ada") }
    }

    @Test
    fun `annotations expose typed arguments`() {
        val record = person().annotation("com.example.Record")!!

        assertEquals("person", record.string("name"))
        assertNull(record.int("name"))
        assertNull(record.annotation("missing"))
    }

    @Test
    fun `annotated lookup returns the declaring class`() {
        KmpReflection.register(module(person()))

        assertEquals(
            listOf("com.example.Person"),
            KmpClasses.annotatedWith("com.example.Record").map { it.qualifiedName },
        )
        assertTrue(KmpClasses.annotatedWith("com.example.Other").isEmpty())
    }

    @Test
    fun `subtype checks follow reflected supertypes and stop at the declaration`() {
        val base = KmpClass("com.example.Base")
        val middle = KmpClass(
            "com.example.Middle",
            supertypes = listOf(KmpTypeRef("com.example.Base")),
            reflectedSupertypes = listOf(base),
        )
        val derived = KmpClass(
            "com.example.Derived",
            supertypes = listOf(KmpTypeRef("com.example.Middle")),
            reflectedSupertypes = listOf(middle),
        )

        assertTrue(derived.isSubclassOf("com.example.Middle"))
        assertTrue(derived.isSubclassOf("com.example.Base"))
        assertTrue(derived.isSubclassOf("Base"))
        assertFalse(derived.isSubclassOf("com.example.Derived"))
        assertFalse(derived.isSubclassOf("com.example.Other"))
    }

    @Test
    fun `all functions deduplicate an inherited override and keep an overload`() {
        val greet = KmpFunction(
            name = "greet",
            returnType = KmpTypeRef("kotlin.String"),
            parameters = listOf(KmpParameter("prefix", KmpTypeRef("kotlin.String"), 0)),
        )
        val greetOverload = KmpFunction(
            name = "greet",
            returnType = KmpTypeRef("kotlin.String"),
            parameters = listOf(KmpParameter("times", KmpTypeRef("kotlin.Int"), 0)),
        )
        val base = KmpClass("com.example.Base", functions = listOf(greet, greetOverload))
        val override = KmpFunction(
            name = "greet",
            returnType = KmpTypeRef("kotlin.String"),
            parameters = listOf(KmpParameter("prefix", KmpTypeRef("kotlin.String"), 0)),
        )
        val derived = KmpClass(
            "com.example.Derived",
            supertypes = listOf(KmpTypeRef("com.example.Base")),
            reflectedSupertypes = listOf(base),
            functions = listOf(override),
        )

        assertEquals(2, derived.allFunctions().size)
        assertEquals("greet", derived.allFunctions().first().name)
        assertEquals(2, base.allFunctions().size)
    }

    @Test
    fun `registration records names and builds metadata on demand`() {
        var loads = 0
        val module = object : KmpReflectModule {
            override val module: String = "lazy"

            // Registration must not read this: the whole point is that nothing is built up front.
            override val classes: List<KmpClass>
                get() = error("`classes` must not be read during registration")

            override val classNames: List<String> = listOf("com.example.A", "com.example.B")

            override fun load(qualifiedName: String): KmpClass? {
                loads++
                return KmpClass(qualifiedName)
            }
        }

        KmpReflection.register(module)
        assertEquals(2, KmpClasses.size)
        assertEquals(0, loads)

        assertEquals("com.example.A", KmpClasses.byName("com.example.A").qualifiedName)
        assertEquals(1, loads)

        // A second lookup is served from the built metadata.
        KmpClasses.byName("com.example.A")
        assertEquals(1, loads)

        // A scan has to build the rest.
        assertEquals(2, KmpClasses.all().size)
        assertEquals(2, loads)
    }

    @Test
    fun `runtime class lookup is answered from the index`() {
        KmpReflection.register(
            module(KmpClass("com.example.Text", simpleName = "Text", runtime = String::class)),
        )

        assertEquals("com.example.Text", KmpClasses.of(String::class).qualifiedName)
        assertFailsWith<KmpReflectException> { KmpClasses.of(Int::class) }
    }

    @Test
    fun `type references answer instance checks`() {
        assertTrue(KmpTypeRef("kotlin.String").isInstance("x"))
        assertFalse(KmpTypeRef("kotlin.String").isInstance(null))
        assertTrue(KmpTypeRef("kotlin.String", nullable = true).isInstance(null))
        // A type whose runtime class was not recorded cannot prove anything, so it accepts.
        assertTrue(KmpTypeRef("kotlin.String").isInstance(1))
    }

    @Test
    fun `implementing follows declared supertypes of registered classes`() {
        val base = KmpClass("com.example.Base")
        val derived = KmpClass("com.example.Derived", supertypes = listOf(KmpTypeRef("com.example.Base")))
        KmpReflection.register(module(derived, base))

        // A class that matches the name itself is included: `implementing` answers "who extends it".
        assertEquals(
            listOf("com.example.Base", "com.example.Derived"),
            KmpClasses.implementing("com.example.Base").map { it.qualifiedName },
        )
        assertTrue(KmpClasses.implementing("com.example.Unregistered").isEmpty())
    }
}
