---
title: Limitations
sidebar_position: 7
description: What v0.1 does not do, and what is planned.
---

# Limitations

Everything here is a decision, not an oversight; each entry names the consequence so you can tell
whether it affects you.

## Model

- **`suspend` invocation needs a coroutine.** A `suspend` function is described and callable, but only
  through `KmpFunction.invokeSuspend` from inside a coroutine: the generated lambda carries the
  `suspend` modifier, so there is no synchronous entry point to hand to a scheduler. The lambda type is
  standard library `suspend` — no coroutines dependency is added.
- **No generic invocation.** A function or constructor whose signature mentions a type parameter
  cannot be called through `Any?` arguments, because the generated call site has no type to cast to.
  It is described instead.
- **No extension members.** Extension functions and properties are not reflected; they are not members
  of the class in the bytecode sense either.
- **No defaults at call time.** Default arguments are not applied by `invoke`: pass every parameter.
- **Supertype links stop at the module boundary.** `superclasses` links the declared supertypes that
  were reflected in the *same module*, and `allSuperclasses` walks that chain. A supertype from another
  artifact stays in `supertypes` only, because its generated metadata is `internal` there; use
  `KmpClasses.implementing(name)` to match by name across everything registered.
- **Annotations are a snapshot.** Values are read from the source at compile time; an annotation whose
  argument is computed elsewhere is recorded as that expression's value only when it is a literal.

## Proxies

- Interfaces only, function-only, no `suspend`, no type parameters; see
  [Dynamic proxy](proxy.md) for the exact rule.
- No properties are forwarded in v0.1, not even concrete ones — a proxy cannot satisfy the compiler
  without also guessing their storage.

## Resources

- MIME types come from a built-in extension table; unknown extensions fall back to
  `application/octet-stream`, and `text()` always decodes as UTF-8.
- Resource directories are not registered as incremental inputs yet, so a stale resource may require
  `--rerun-tasks`.

## Platforms

- **Reified lookups depend on what the backend knows about `KClass`.** The JVM exposes qualified names, so
  `KmpClasses.of<T>()`, `annotatedWith<A>()` and `implementing(K::class)` are exact there. Kotlin/Native
  exposes simple names only, so those three match by simple name; Kotlin/JS and Kotlin/Wasm expose no
  names at all, so they resolve by identity or return empty. Metadata always carries the qualified name:
  use the string overloads (`byName`, `annotatedWith(name)`, `implementing(name)`) when it matters.
- **A native target is declared per build host.** This repository builds one native target that the
  host can link and run (`mingwX64`, `linuxX64` or the Apple ones). Consuming a published klib for
  another native target works; building one requires a matching host.
- **Android is covered by the JVM artifact.** There is no `androidTarget` publication yet: an Android
  application consumes the JVM variant, which is why the runtime has no JDK-only API.

## API surface

- Registration is global and last-write-wins per qualified name. There is no scoping, and `clear()`
  exists mainly for tests.
- `KmpInvocationHandler` gets positional arguments only; named arguments, defaults and varargs
  semantics are the caller's business.

## Candidates for v0.2

1. A Gradle plugin that declares resource directories as KSP inputs and wires the options per target,
   including looping over every declared target.
2. Proxy support for concrete properties (backed by a supplied map).
3. Recursive reflection of supertypes that belong to the same module.
4. An `androidTarget` publication, so Android consumers get a variant of their own instead of falling
   back to the JVM one.
