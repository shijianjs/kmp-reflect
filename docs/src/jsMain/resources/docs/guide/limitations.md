---
title: Limitations
sidebar_position: 7
description: What v0.1 does not do, and what is planned.
---

# Limitations

Everything here is a decision, not an oversight; each entry names the consequence so you can tell
whether it affects you.

## Model

- **No `suspend` invocation.** A `suspend` function is described but not callable. Launching it would
  need a `CoroutineScope`, and choosing one for the caller is not something metadata should do.
- **No generic invocation.** A function or constructor whose signature mentions a type parameter
  cannot be called through `Any?` arguments, because the generated call site has no type to cast to.
  It is described instead.
- **No extension members.** Extension functions and properties are not reflected; they are not members
  of the class in the bytecode sense either.
- **No defaults at call time.** Default arguments are not applied by `invoke`: pass every parameter.
- **No transitive supertypes.** `supertypes` lists what was declared. Use
  `KmpClasses.implementing(name)` when you need the full chain over registered classes.
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

- Kotlin/Native and Kotlin/Wasm are not supported, because KSP cannot feed generated sources into those
  compilations.
- On Kotlin/JS, reified lookups (`KmpClasses.of<T>()`, `annotatedWith<A>()`) fall back to identity or
  return empty: the platform exposes no runtime class names. String-based lookups are exact
  everywhere.

## API surface

- Registration is global and last-write-wins per qualified name. There is no scoping, and `clear()`
  exists mainly for tests.
- `KmpInvocationHandler` gets positional arguments only; named arguments, defaults and varargs
  semantics are the caller's business.

## Candidates for v0.2

1. A Gradle plugin that declares resource directories as KSP inputs and wires the options per target.
2. Proxy support for concrete properties (backed by a supplied map).
3. `suspend` invocation with a caller-provided `CoroutineScope`.
4. Recursive reflection of supertypes that belong to the same module.
5. Kotlin/Native support if KSP grows a compilation task that accepts generated sources there.
