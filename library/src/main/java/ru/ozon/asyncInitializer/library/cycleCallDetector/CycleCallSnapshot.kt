package ru.ozon.asyncInitializer.library.cycleCallDetector

/**
 * Cнимок CallStack [ru.ozon.asyncInitializer.library.ComponentInitializer],
 * которые могли вызвать друг друга в момент инициализации
 */
internal class CycleCallSnapshot private constructor(
    private val orderedVisitedInitializers: List<InitializerCallStackFrame>,
) {

    fun UniqueInitializerCallStack.Companion.restore(): UniqueInitializerCallStack {
        return create(orderedVisitedInitializers.iterator())
    }

    companion object {
        fun instance(orderedVisitedInitializers: UniqueInitializerCallStack): CycleCallSnapshot {
            return CycleCallSnapshot(orderedVisitedInitializers.toList())
        }
    }
}
