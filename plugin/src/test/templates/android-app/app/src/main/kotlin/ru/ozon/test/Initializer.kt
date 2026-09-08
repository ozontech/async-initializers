package ru.ozon.test

import ru.ozon.android.componentInitiazlizer.ComponentInitializer

/**
 * Инициалайзер, на который ссылается inject.config.
 * Валидация ищет класс по имени из конфига.
 */
class InitializerA : ComponentInitializer() {

    override fun runInitialize() {
        // тело не важно для проверки патчинга
    }
}