# AsyncInitializers

**English** | [Русский](../library/README.ru.md)

## 📋 Table of contents
- [ℹ️ Description](#-description)
- [🔌️ Setup](#-setup)
- [🛠️ Configuration and initialization](#-configuration-and-initialization)
- [🚀 Usage](#-usage)

## ℹ️ Description

`AsyncInitializer` — a library for safe and thread-safe initialization of application components. It provides a framework for managing dependencies during initialization with the following guarantees:

- **Thread-safe initialization** — Double-Check Locking guarantees that a component's initialization is invoked exactly once regardless of the number of threads
- **Cyclic dependency detection** — automatic detection of cycles like A → B → A based on a thread-local call stack
- **MainThread support** — the ability to force initialization to run on the main thread with automatic switching from any thread
- **Deadlock protection** — optimized synchronization for MainThread that prevents threads from blocking each other

The library is designed for large multi-module Android applications where components are initialized on demand (lazy initialization) and may have a complex dependency graph.

## 🔌️ Setup

##### libs.toml
```toml
[versions]
asyncInitializersVersion = "1.0.0"

[libraries]
asyncInitializers = { module = "ru.ozon:async-initializers", version.ref = "asyncInitializersVersion" }
```
##### build.gradle.kts
```kts
dependencies {
    implementation(libs.asyncInitializers)
}
```

Or directly, without the version catalog:

```kts
dependencies {
    implementation("ru.ozon:async-initializers:1.0.0")
}
```

## 🛠️ Configuration and initialization

Before use, you need to configure `AppComponentInitializerProvider` once at the application entry point:

```kotlin
// We create a components factory (implements AppComponentInitializerFactory)
class MyComponentsFactory : AppComponentInitializerFactory {
    override fun <T : ComponentInitializer> create(initializer: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when (initializer) {
            AnalyticsInitializer::class.java -> AnalyticsInitializer()
            DatabaseInitializer::class.java -> DatabaseInitializer()
            // ...
            else -> error("Unknown initializer: ${initializer.simpleName}")
        } as T
    }
}

// We configure the provider (called once at application startup)
setupAppComponentInitializer(
    factory = MyComponentsFactory(),
)
```

By default, the `DefaultComponentInitializerStore` (an in-memory cache) and the `DefaultPlatformMainThread` (based on `Handler(Looper.getMainLooper())`) are used. If needed, they can be overridden:

```kotlin
setupAppComponentInitializer(
    factory = MyComponentsFactory(),
    store = MyCustomStore(),
    platformMainThread = MyPlatformMainThread(),
)
```

## 🚀 Usage

### Creating a component initializer

```kotlin
class AnalyticsInitializer : ComponentInitializer() {

    override fun runInitialize() {
        // Heavy component initialization
    }
}
```

### Running on the MainThread

```kotlin
class UiComponentsInitializer : ComponentInitializer(runOnlyOnMainThread = true) {

    override fun runInitialize() {
        // Runs on the MainThread
    }
}
```

### Managing dependencies

Dependencies are resolved manually inside `runInitialize()` via `getComponentInitializer<T>()`:

```kotlin
class AnalyticsInitializer : ComponentInitializer() {

    override fun runInitialize() {
        // Explicit dependency resolution
        getComponentInitializer<DatabaseInitializer>().initialize()
        getComponentInitializer<NetworkInitializer>().initialize()
    }
}
```

### Running initialization and getting a component

```kotlin
// Getting the initializer by type (reified)
val analyticsInitializer = getComponentInitializer<AnalyticsInitializer>()

// Running initialization (with a single-invocation guarantee)
analyticsInitializer.initialize()
```

On the first call to `getComponentInitializer` the factory creates an instance, which is cached in the `ComponentInitializerStore`. On subsequent requests the cached instance is returned.

### Extending capabilities with the component-injector plugin

Component-initializer provides an additional solution for inserting calls of the form

```kotlin
getComponentInitializer<NetworkInitializer>().initialize()
```

into places that are not under developer control (for example, inside libraries).

To learn more and set it up, use the dedicated plugin [async-initializer-Injector](../plugin/README.md).

## License
The AsyncInitializers library is distributed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html) and is freely available on GitHub.