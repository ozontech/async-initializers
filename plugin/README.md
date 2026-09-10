# AsyncInitializers Injector Plugin

**English** | [Русский](../plugin/README.ru.md)

A plugin for bytecode patching of Android applications: it automatically inlines a call
to the initializer (`ComponentInitializer`) into the public methods of the specified classes
directly at build time, without manual calls in the code.

## ℹ️ Description

In applications it is often required that a component is already initialized at the entry point
of a feature. The plugin injects a call to `getComponentInitializer(T::class.java).initialize()`
into the public methods of the target classes at build time, without requiring any changes
to the source code.

Benefits:

- initialization of components without modifying source code;
- injection of initializers into libraries to which you have no access;
- calls are not scattered across the codebase — everything is described in a single config file.

Example.
Before applying the plugin:

```kotlin
// Target class source code
object ComponentA {
    fun initialize(message: String) {
        this.message = message
    }
}
```

becomes:

```kotlin
object ComponentA {
    fun initialize(message: String) {
        getComponentInitializer(InitializerA::class.java).initialize() // injected by the plugin
        this.message = message
    }
}
```

## 📦 Setup

```toml
# gradle/libs.versions.toml
[versions]
asyncInitializerInjectorVersion = "1.0.0"

[plugins]
asyncInitializerInjector = { id = "ru.ozon.asyncInitializer-injector", version.ref = "asyncInitializerInjectorVersion" }
```

```kotlin
// Root build.gradle.kts
plugins {
    alias(libs.plugins.asyncInitializerInjector) apply false
}
```

```kotlin
// build.gradle.kts in the application module (the plugin works only with com.android.application)
plugins {
    id("com.android.application")
    alias(libs.plugins.asyncInitializerInjector)
}
```

## 📚 Runtime library

The plugin is an **add-on** to the runtime library. It works only with initializer classes that inherit
from the base class `ComponentInitializer` from the runtime library
`ru.ozon:asyncInitializers`.

Setup and usage of the runtime library: [library/README.md](../library/README.md).

> The initializer must inherit from the base class `ComponentInitializer`.

```kotlin
import ru.ozon.asyncInitializer.library.ComponentInitializer

class InitializerA : ComponentInitializer() {
    override fun runInitialize() {
        // Initialization logic
    }
}
```

## 🛠️ Configuration

The plugin uses the `injectInitializerComponents` container, in which a separate configuration
is declared for each buildType (`debug`, `release`, etc.).

> The plugin is applied to a variant only if the container name matches the variant's `buildType`
> (configuration is not split by product flavors).

```kotlin
// build.gradle.kts in the application module
injectInitializerComponents {
    create("debug") {
        // Files describing which initializers to inject into which classes
        configs = listOf(layout.projectDirectory.file("initializer.config"))

        // Verify after patching that all declared classes were found and patched.
        // In the current implementation this is a slow procedure (false by default)
        isTransformResultEnabled = true
    }
}
```

### Config file format

The config format is an initializer → target-class pair:

```
inject <InitializerClass> toPublicMethods <TargetClass> {
    ignore fun <methodName>(<parameters>): <returnType>?
}
```

Elements of the `inject` directive:

- `inject` — the fully qualified name of the initializer class (a subclass of `ComponentInitializer` from the runtime library).
- `toPublicMethods` — the fully qualified name of the target class, into whose public methods the call is injected.
- Inside the `{}` block the `ignore` methods that **must not** be patched are listed. (It can be left empty.)

The `ignore` directive is matched against a method of the target class **by its full signature** — name,
parameters, and return type — so the signature must be specified exactly as it appears in the code:

- `methodName` — the method name;
- `parameters` — parameter types separated by commas (can be left empty if there are no parameters);
- `: returnType` — the return type (can be omitted if the method returns `Unit`).

#### Primitive types

Reserved names are used for primitives, `Unit`, and `String`: `Int`, `Long`, `Short`, `Byte`,
`Boolean`, `Char`, `Float`, `Double`, `Unit`. `String` is also
reserved and corresponds to `java.lang.String`.

```text
inject ru.ozon.example.InitializerA toPublicMethods ru.ozon.example.ComponentA {
    ignore fun calculate(Int, Long): Int
    ignore fun isReady(): Boolean
    ignore fun process(String, Boolean)
    ignore fun clear()
}
```

#### Object types

For all other types, the fully qualified class name is specified (your class or a library class) —
both in the parameters and in the return type.

```text
inject ru.ozon.example.InitializerB toPublicMethods ru.ozon.example.ComponentB {
    ignore fun configure(ru.ozon.example.api.AppConfig, ru.ozon.example.api.AppDependencies)
    ignore fun build(ru.ozon.example.Args): ru.ozon.example.Result
    ignore fun load(): ru.ozon.example.Config
}
```

#### Mixed types

Parameters of different types can be combined freely. A complete valid example of a config file:

```text
inject ru.ozon.example.componentInitializers.InitializerA toPublicMethods ru.ozon.example.componentInitializers.ComponentA { }
inject ru.ozon.example.componentInitializers.InitializerB toPublicMethods ru.ozon.example.componentInitializers.ComponentB {
    ignore fun get(): String
}
inject ru.ozon.example.componentInitializers.InitializerC toPublicMethods ru.ozon.example.componentInitializers.ComponentC {
    ignore fun update(ru.ozon.example.Model, Boolean, Long): ru.ozon.example.Status
}
```

> **Important:** the `ignore` method signature must match the real signature of the method
> in the target class, otherwise the build will fail at the validation stage.

#### License

The AsyncInitializers Injector Plugin is distributed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html) and is freely available on GitHub. The AsyncInitializers Injector Plugin distribution includes the ASM library, released under the [3-Clause BSD License](https://asm.ow2.io/license.html).