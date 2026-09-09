package ru.ozon.asyncInitializer.library.cycleCallDetector

/**
 * Snapshot of the CallStack of [ru.ozon.asyncInitializer.library.ComponentInitializer],
 * that may have called each other during initialization
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
