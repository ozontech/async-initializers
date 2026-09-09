package ru.ozon.asyncInitializer.library.cycleCallDetector

import ru.ozon.asyncInitializer.library.emptyIterator
import ru.ozon.asyncInitializer.library.exceptions.CycleComponentInitializerException

/**
 * Unique CallStack of [ru.ozon.asyncInitializer.library.ComponentInitializer]
 * prevents keeping two identical initializers in the CallStack
 */
internal class UniqueInitializerCallStack private constructor(
    initializeStack: Iterator<InitializerCallStackFrame>,
) : Iterable<InitializerCallStackFrame> {
    private val stack = ArrayDeque<InitializerCallStackFrame>()

    init {
        initializeStack.forEach { frame -> stack.addLast(frame) }
    }

    fun isEmpty(): Boolean = stack.isEmpty()

    fun push(frame: InitializerCallStackFrame) {
        if (stack.contains(frame)) {
            throw CycleComponentInitializerException(
                order = stack.map { it.initializerClass.simpleName },
                repeatedClass = frame.initializerClass.simpleName,
            )
        }
        stack.addLast(frame)
    }

    fun pop() {
        stack.removeLastOrNull() ?: throw NoSuchElementException("Attempt to remove an element from an empty initializer stack")
    }

    fun peek(): InitializerCallStackFrame? {
        return stack.lastOrNull()
    }

    override fun iterator(): Iterator<InitializerCallStackFrame> = stack.iterator()

    companion object {
        fun create(
            initializeStack: Iterator<InitializerCallStackFrame> = emptyIterator(),
        ): UniqueInitializerCallStack = UniqueInitializerCallStack(initializeStack)
    }
}
