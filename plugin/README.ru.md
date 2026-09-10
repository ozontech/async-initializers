# AsyncInitializers Injector Plugin

[English](../plugin/README.md) | **Русский**

Плагин для патчинга байткода Android-приложений: автоматически встраивает вызов
инициалайзера (`ComponentInitializer`) в публичные методы указанных классов напрямую
на этапе сборки, без ручных вызовов в коде.

## ℹ️ Описание

В приложениях часто требуется в точке входа в фичу быть уверенным, что компонент уже
инициализирован. Плагин внедряет вызов `getComponentInitializer(T::class.java).initialize()`
в публичные методы target-классов на этапе сборки, не требуя изменений в исходном коде.

Плюсы:

- инициализация компонентов без изменения исходного кода;
- инжект инициалайзеров в библиотеки, к которым нет доступа;
- вызовы не размазаны по кодовой базе — всё описывается одним конфиг-файлом.

Пример. 
До применения плагина:

```kotlin
// Исходный код target-класса
object ComponentA {
    fun initialize(message: String) {
        this.message = message
    }
}
```

превращается в:

```kotlin
object ComponentA {
    fun initialize(message: String) {
        getComponentInitializer(InitializerA::class.java).initialize() // встроено плагином
        this.message = message
    }
}
```

## 📦 Подключение

```toml
# gradle/libs.versions.toml
[versions]
asyncInitializerInjectorVersion = "1.0.0"

[plugins]
asyncInitializerInjector = { id = "ru.ozon.asyncInitializer-injector", version.ref = "asyncInitializerInjectorVersion" }
```

```kotlin
// Корневой build.gradle.kts
plugins {
    alias(libs.plugins.asyncInitializerInjector) apply false
}
```

```kotlin
// build.gradle.kts в application-модуле (плагин работает только с com.android.application)
plugins {
    id("com.android.application")
    alias(libs.plugins.asyncInitializerInjector)
}
```

## 📚 Рантайм-библиотека

Плагин является **дополнением** к рантайм-библиотеке и работает только с классами-инициалайзерами, которые наследуются
от базового класса `ComponentInitializer` из рантайм-библиотеки
`ru.ozon:asyncInitializers`.

Подключение и использование рантайм-библиотеки: [library/README.md](../library/README.ru.md).

> Инициалайзер обязательно должен наследоваться от базового класса `ComponentInitializer`.

```kotlin
import ru.ozon.asyncInitializer.library.ComponentInitializer

class InitializerA : ComponentInitializer() {
    override fun runInitialize() {
        // Логика инициализации
    }
}
```

## 🛠️ Настройка

Плагин использует контейнер `injectInitializerComponents`, в котором для каждого
buildType (`debug`, `release` и т.д.) объявляется своя конфигурация.

> Плагин применяется к варианту, только если имя контейнера совпадает с `buildType`
> варианта (конфигурация по продукт-флейворам не разделяется).

```kotlin
// build.gradle.kts в application-модуле
injectInitializerComponents {
    create("debug") {
        // Файлы с описанием, какие инициалайзеры встроить в какие классы
        configs = listOf(layout.projectDirectory.file("initializer.config"))

        // Проверять после патчинга, что все заявленные классы найдены и пропатчены.
        // В текущей реализации — долгая процедура (по умолчанию false)
        isTransformResultEnabled = true
    }
}
```

### Формат конфиг-файла

Формат конфига пара «инициалайзер → target класс»:

```
inject <InitializerClass> toPublicMethods <TargetClass> {
    ignore fun <methodName>(<parameters>): <returnType>?
}
```

Элементы директивы `inject`:

- `inject` — полное имя класса-инициалайзера (наследник `ComponentInitializer` из рантайм-библиотеки).
- `toPublicMethods` — полное имя target-класса, в публичные методы которого встраивается вызов.
- Внутри блока `{}` перечисляются `ignore`-методы, которые патчить **не** нужно (можно оставлять пустым).

Директива `ignore` сопоставляется с методом target-класса **по полной сигнатуре** — имя,
параметры и возвращаемый тип, поэтому сигнатуру нужно указывать точно так же, как в коде:

- `methodName` — имя метода;
- `parameters` — типы параметров через запятую (можно оставить пустым, если параметров нет);
- `: returnType` — возвращаемый тип (можно опустить, если метод возвращает `Unit`).

#### Примитивные типы

Для примитивов, Unit, String используются зарезервированные имена: `Int`, `Long`, `Short`, `Byte`,
`Boolean`, `Char`, `Float`, `Double`, `Unit`. `String` также
зарезервирован и соответствует `java.lang.String`.

```text
inject ru.ozon.example.InitializerA toPublicMethods ru.ozon.example.ComponentA {
    ignore fun calculate(Int, Long): Int
    ignore fun isReady(): Boolean
    ignore fun process(String, Boolean)
    ignore fun clear()
}
```

#### Типы-объекты

Для остальных типов указывается полное имя класса (ваш класс или класс библиотеки) —
как в параметрах, так и в возвращаемом типе.

```text
inject ru.ozon.example.InitializerB toPublicMethods ru.ozon.example.ComponentB {
    ignore fun configure(ru.ozon.example.api.AppConfig, ru.ozon.example.api.AppDependencies)
    ignore fun build(ru.ozon.example.Args): ru.ozon.example.Result
    ignore fun load(): ru.ozon.example.Config
}
```

#### Смешанные типы

Параметры разных типов свободно комбинируются. Полный корректный пример конфиг-файла:

```text
inject ru.ozon.example.componentInitializers.InitializerA toPublicMethods ru.ozon.example.componentInitializers.ComponentA { }
inject ru.ozon.example.componentInitializers.InitializerB toPublicMethods ru.ozon.example.componentInitializers.ComponentB {
    ignore fun get(): String
}
inject ru.ozon.example.componentInitializers.InitializerC toPublicMethods ru.ozon.example.componentInitializers.ComponentC {
    ignore fun update(ru.ozon.example.Model, Boolean, Long): ru.ozon.example.Status
}
```

> **Важно:** сигнатура `ignore`-метода должна совпасть с реальной сигнатурой метода
> в target-классе, иначе сборка упадёт на этапе валидации.

#### Лицензия

AsyncInitializers Injector Plugin распространяется по лицензии [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) и находится в свободном доступе на GitHub. Дистрибутив AsyncInitializers Injector Plugin включает в себя библиотеку ASM, выпущенную под [лицензией 3-Clause BSD](https://asm.ow2.io/license.html).
