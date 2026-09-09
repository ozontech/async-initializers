# AsyncInitializers

## 📋 Содержание
- [ℹ️ Описание](#ℹ-описание)
- [🔌️ Подключение](#-подключение)
- [🛠️ Настройка и инициализация](#-настройка-и-инициализация)
- [🚀 Использование](#-использование)
- [📞 Контакты](#-контакты)

## ℹ️ Описание

`AsyncInitializer` — библиотека для безопасной и потокобезопасной инициализации компонентов приложения. Предоставляет фреймворк для управления зависимостями при инициализации с гарантиями:

- **Thread-safe инициализация** — двойная проверка блокировки (Double-Check Locking) гарантирует однократный вызов инициализации компонента независимо от количества потоков
- **Детектирование циклических зависимостей** — автоматическое обнаружение циклов вида A → B → A на основе thread-local стека вызовов
- **Поддержка MainThread** — возможность принудительного выполнения инициализации на главном потоке с автоматическим переключением из любого потока
- **Защита от дедлоков** — оптимизированная синхронизация для MainThread, предотвращающая взаимную блокировку потоков

Библиотека рассчитана на использование в крупных многомодульных Android-приложениях, где компоненты инициализируются по требованию (lazy initialization) и могут иметь сложный граф зависимостей.
## 🔌️ Подключение

##### libs.toml
```toml
[versions]
asyncInitializersVersion = "1.0.0"

[libraries]
asyncInitializers = { module = "ru.ozon:asyncInitializers", version.ref = "asyncInitializersVersion" }
```
##### build.gradle.kts
```kts
dependencies {
    implementation(libs.asyncInitializers)
}
```

Либо напрямую, без каталога версий:

```kts
dependencies {
    implementation("ru.ozon:asyncInitializers:1.0.0")
}
```

## 🛠️ Настройка и инициализация

Перед использованием необходимо однократно настроить `AppComponentInitializerProvider` в точке входа приложения:

```kotlin
// Создаём фабрику компонентов (реализует AppComponentInitializerFactory)
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

// Настраиваем провайдер (вызывается один раз при старте приложения)
setupAppComponentInitializer(
    factory = MyComponentsFactory(),
)
```

По умолчанию используется `DefaultComponentInitializerStore` (in-memory кэш) и `DefaultPlatformMainThread` (на основе `Handler(Looper.getMainLooper())`). При необходимости их можно переопределить:

```kotlin
setupAppComponentInitializer(
    factory = MyComponentsFactory(),
    store = MyCustomStore(),
    platformMainThread = MyPlatformMainThread(),
)
```

## 🚀 Использование

### Создание инициализатора компонента

```kotlin
class AnalyticsInitializer : ComponentInitializer() {

    override fun runInitialize() {
        // Тяжёлая инициализация компонента
    }
}
```

### Выполнение на MainThread

```kotlin
class UiComponentsInitializer : ComponentInitializer(runOnlyOnMainThread = true) {

    override fun runInitialize() {
        // Выполняется на MainThread
    }
}
```

### Управление зависимостями

Зависимости разрешаются вручную внутри `runInitialize()` через `getComponentInitializer<T>()`:

```kotlin
class AnalyticsInitializer : ComponentInitializer() {

    override fun runInitialize() {
        // Явное разрешение зависимостей
        getComponentInitializer<DatabaseInitializer>().initialize()
        getComponentInitializer<NetworkInitializer>().initialize()
    }
}
```

### Запуск инициализации и получение компонента

```kotlin
// Получение инициализатора по типу (reified)
val analyticsInitializer = getComponentInitializer<AnalyticsInitializer>()

// Запуск инициализации (с гарантией однократного вызова)
analyticsInitializer.initialize()
```

При первом обращении к `getComponentInitializer` фабрика создаёт экземпляр, который кэшируется в `ComponentInitializerStore`. При повторном запросе возвращается закэшированный экземпляр.

### Расширение возможностей при использовании component-initializer

Component-initializer имеет дополнительное решение для вставки вызовов вида

```kotlin
getComponentInitializer<NetworkInitializer>().initialize()
```

в места, которые не подконтрольны разработчику (например, внутри библиотек).

Для ознакомления и подключения используйте специальный плагин [asyncInitializer-injector](../plugin/README.md).

## Лицензия
Библиотека AsyncInitializers распространяется по лицензии [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) и находится в свободном доступе на GitHub.