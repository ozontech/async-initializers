package ru.ozon.asyncInitializer.library

/**
 * !!! НЕ УДАЛЯТЬ
 *
 * Аннотация используется как маркер при патчинге байт-кода над функциями, в которые был вставлен вызов [ComponentInitializer]
 */
@Suppress("unused")
internal annotation class WasInjectInitializer
