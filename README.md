# AsyncInitializers

**English** | [Русский](README.ru.md)

A repository with solutions for safe and accelerated initialization of dependencies in Android applications:
a runtime library with an initialization framework and a Gradle plugin for its seamless integration.

## 📦 Components

| Component | Purpose |
| --- | --- |
| [`library/`](library/README.md) | `AsyncInitializer` runtime library — thread-safe component initialization, cyclic dependency detection, MainThread support, and deadlock protection. |
| [`plugin/`](plugin/README.md) | `AsyncInitializers Injector` Gradle plugin — bytecode patching: automatically inlines initializer calls into public methods of classes at build time. |

## 🔗 Documentation

- [📚 Runtime library `library/README.md`](library/README.md)
- [💉 Gradle plugin `plugin/README.md`](plugin/README.md)

## License

The AsyncInitializers library and the AsyncInitializers Injector Plugin are distributed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html) and are freely available on GitHub. The AsyncInitializers Injector Plugin distribution includes the ASM library, released under the [3-Clause BSD License](https://asm.ow2.io/license.html).