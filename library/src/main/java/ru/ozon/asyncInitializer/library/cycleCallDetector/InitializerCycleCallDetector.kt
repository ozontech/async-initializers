package ru.ozon.asyncInitializer.library.cycleCallDetector

import ru.ozon.asyncInitializer.library.ComponentInitializer
import kotlin.concurrent.getOrSet

/**
 * Детектор обнаружения циклов при вызове инициалайзеров
 *
 * Гарантирует обнаружение цикличности (AInitializer -> BInitializer -> AInitializer)
 * в рамках одного потока
 *
 */
internal class InitializerCycleCallDetector private constructor() {
    private val initializerCallStackThreadLocal = ThreadLocal<UniqueInitializerCallStack>()

    private val currentUniqueInitializerCallStack: UniqueInitializerCallStack
        get() = initializerCallStackThreadLocal.getOrSet { UniqueInitializerCallStack.create() }

    /**
     * Детект повторного вызова инициалайзеров
     *
     * @param componentInitializer инициалайзер, повторный вызов которого будет обнаружен
     * при цикличной инициализации
     */
    fun detectOnCycle(
        componentInitializer: ComponentInitializer,
        action: () -> Unit,
    ) {
        val local = currentUniqueInitializerCallStack
        val frame = InitializerCallStackFrame(componentInitializer)

        local.push(frame)
        try {
            action()
        } finally {
            local.pop()
            if (local.isEmpty()) initializerCallStackThreadLocal.remove()
        }
    }

    /**
     * Создание нового стека инициализации для корректной работы детектора
     *
     * Полезен в ситуации смены потока, когда нужно прошлый CallStack [ComponentInitializer] перенести в другой поток
     *
     * @param cycleCallSnapshot снимок прошлого CallStack [ComponentInitializer]
     */
    fun continueDetectFrom(
        cycleCallSnapshot: CycleCallSnapshot,
        action: () -> Unit,
    ) {
        val local = with(cycleCallSnapshot) { UniqueInitializerCallStack.restore() }

        val previous = initializerCallStackThreadLocal.get()
        initializerCallStackThreadLocal.set(local)
        try {
            action()
        } finally {
            when (previous) {
                null -> initializerCallStackThreadLocal.remove()
                else -> initializerCallStackThreadLocal.set(previous)
            }
        }
    }

    /**
     * Создает снимок CallStack [ComponentInitializer]
     */
    fun takeCycleCallSnapshot(): CycleCallSnapshot {
        return CycleCallSnapshot.Companion.instance(currentUniqueInitializerCallStack)
    }

    fun isRepeatCall(componentInitializer: ComponentInitializer): Boolean {
        val local = currentUniqueInitializerCallStack
        val frame = InitializerCallStackFrame(componentInitializer)

        return local.peek() == frame
    }

    companion object {
        fun create(): InitializerCycleCallDetector = InitializerCycleCallDetector()
    }
}
