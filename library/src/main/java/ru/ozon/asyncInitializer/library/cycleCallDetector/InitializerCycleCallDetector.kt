package ru.ozon.asyncInitializer.library.cycleCallDetector

import ru.ozon.asyncInitializer.library.ComponentInitializer
import kotlin.concurrent.getOrSet

/**
 * Cycle detector for initializer invocations
 *
 * Guarantees cycle detection (AInitializer -> BInitializer -> AInitializer)
 * within a single thread
 *
 */
internal class InitializerCycleCallDetector private constructor() {
    private val initializerCallStackThreadLocal = ThreadLocal<UniqueInitializerCallStack>()

    private val currentUniqueInitializerCallStack: UniqueInitializerCallStack
        get() = initializerCallStackThreadLocal.getOrSet { UniqueInitializerCallStack.create() }

    /**
     * Detects repeated invocation of initializers
     *
     * @param componentInitializer the initializer whose repeated call will be detected
     * during cyclic initialization
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
     * Creates a new initialization stack for correct detector operation
     *
     * Useful when switching threads to move the previous CallStack of [ComponentInitializer] to another thread
     *
     * @param cycleCallSnapshot the snapshot of the previous CallStack of [ComponentInitializer]
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
     * Creates a snapshot of the CallStack of [ComponentInitializer]
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
