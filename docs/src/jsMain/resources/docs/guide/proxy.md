---
title: Dynamic proxy
sidebar_position: 4
description: Interfaces get a generated proxy class that forwards every call to your handler.
---

# Dynamic proxy

The JVM's `Proxy.newProxyInstance` does not exist on other targets. Here, the processor writes an
implementation of the interface instead — a real Kotlin class, compiled with your code.

## Requirements

A proxy is generated when **all** of these hold:

- the interface is reflected (`@KmpReflect` or `KmpReflectable`);
- it has at least one abstract function;
- it declares **no** properties at all — not even concrete ones;
- every abstract member is declared on the interface itself, not inherited from an unreflected supertype;
- no member is `suspend` and no member mentions a type parameter.

When a condition fails, no proxy class is written, `KmpClass.isProxyable` is `false`, `newProxy` throws
`KmpReflectException`, and the processor prints a warning naming the reason.

## Using one

```kotlin
@KmpReflect
fun interface Greeter {
    fun greet(name: String): String
}
```

```kotlin
val handler = KmpInvocationHandler { proxy, function, arguments ->
    "hello, ${arguments.first()}"
}

val greeter = KmpClasses.of<Greeter>().newProxy(handler) as Greeter
greeter.greet("world")      // "hello, world"
```

`KmpProxy.create<Greeter> { _, function, args -> … }` is the same thing without a lookup first.

The handler receives:

| Parameter | Meaning |
| --- | --- |
| `proxy` | the proxy instance, for `equals`/`toString`-style self reference |
| `function` | the `KmpFunction` the call maps to, with its metadata |
| `arguments` | positional arguments, already cast to the declared types |

Its return value is cast to the declared return type; a `Unit` function ignores it. A wrong type,
or a handler that returns `null` for a non-null return type, fails at the call site — the proxy does
not hide it.

## What a proxy is not

- Not a subclass factory: it implements interfaces only, and only for members the metadata describes.
- Not stateful: the proxy holds the handler and the reflected class, nothing else. Anything else lives
  in your handler's closure.
- Not a mock framework: no stubbing, no verification, no argument matchers. It is the mechanism; build
  the framework on top if you need one.
- Not eager: nothing is instantiated until `newProxy` is called.
