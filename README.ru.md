# AsyncInitializers

[English](README.md) | **Русский**

Репозиторий с решениями для безопасной и ускоренной инициализации зависимостей в Android-приложениях:
рантайм-библиотека с фреймворком инициализации и Gradle-плагин для её бесшовной интеграции.

## 📦 Компоненты

| Компонент | Назначение |
| --- | --- |
| [`library/`](library/README.ru.md) | Рантайм-библиотека `AsyncInitializer` — потокобезопасная инициализация компонентов, детектирование циклических зависимостей, поддержка MainThread и защита от дедлоков. |
| [`plugin/`](plugin/README.ru.md) | Gradle-плагин `AsyncInitializers Injector` — патчинг байткода: автоматически встраивает вызовы инициалайзеров в публичные методы классов на этапе сборки. |

## 🔗 Документация

- [📚 Рантайм-библиотека `library/README.md`](library/README.ru.md)
- [💉 Gradle-плагин `plugin/README.md`](plugin/README.ru.md)

## Лицензия

Библиотека AsyncInitializers и плагин AsyncInitializers Injector Plugin распространяются по лицензии [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) и находятся в свободном доступе на GitHub. Дистрибутив AsyncInitializers Injector Plugin включает в себя библиотеку ASM, выпущенную под [лицензией 3-Clause BSD](https://asm.ow2.io/license.html).