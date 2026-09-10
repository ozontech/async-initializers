package ru.ozon.asyncInitializer

import ru.ozon.asyncInitializer.library.ComponentInitializer
import ru.ozon.asyncInitializer.library.cycleCallDetector.CycleCallSnapshot
import ru.ozon.asyncInitializer.library.cycleCallDetector.InitializerCallStackFrame
import ru.ozon.asyncInitializer.library.cycleCallDetector.UniqueInitializerCallStack
import ru.ozon.asyncInitializer.library.exceptions.CycleComponentInitializerException
import ru.ozon.asyncInitializer.util.FuncComponentInitializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UniqueInitializerCallStackTest {

    private fun frame(initializer: ComponentInitializer) = InitializerCallStackFrame(initializer)

    @Test
    fun emptyStackIsEmpty() {
        val stack = UniqueInitializerCallStack.create()
        assertTrue(stack.isEmpty())
        assertEquals(null, stack.peek())
    }

    @Test
    fun pushPopPeekWorksInOrder() {
        val stack = UniqueInitializerCallStack.create()
        val a = FuncComponentInitializer {}
        val b = FuncComponentInitializer {}
        val c = FuncComponentInitializer {}

        stack.push(frame(a))
        assertFalse(stack.isEmpty())
        assertEquals(frame(a), stack.peek())

        stack.push(frame(b))
        stack.push(frame(c))
        assertEquals(frame(c), stack.peek())

        stack.pop()
        assertEquals(frame(b), stack.peek())

        stack.pop()
        assertEquals(frame(a), stack.peek())

        stack.pop()
        assertTrue(stack.isEmpty())
        assertEquals(null, stack.peek())
    }

    @Test
    fun iteratorReturnsFramesInOrder() {
        val stack = UniqueInitializerCallStack.create()
        val a = FuncComponentInitializer {}
        val b = FuncComponentInitializer {}
        stack.push(frame(a))
        stack.push(frame(b))

        val visited = stack.map { it }
        assertEquals(listOf(frame(a), frame(b)), visited)
    }

    @Test
    fun pushDuplicateThrowsCycleComponentInitializerException() {
        val a = FuncComponentInitializer {}
        val b = FuncComponentInitializer {}
        val stack = UniqueInitializerCallStack.create()
        stack.push(frame(a))
        stack.push(frame(b))

        val exception = assertFailsWith<CycleComponentInitializerException> {
            stack.push(frame(a))
        }

        val expectedMessage = CycleComponentInitializerException(
            order = listOf(a::class.java.simpleName, b::class.java.simpleName),
            repeatedClass = a::class.java.simpleName,
        ).message
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun popFromEmptyStackThrows() {
        val stack = UniqueInitializerCallStack.create()
        assertFailsWith<NoSuchElementException> {
            stack.pop()
        }
    }

    @Test
    fun restoreFromSnapshotKeepsOrder() {
        val stack = UniqueInitializerCallStack.create()
        val a = FuncComponentInitializer {}
        val b = FuncComponentInitializer {}
        val c = FuncComponentInitializer {}
        stack.push(frame(a))
        stack.push(frame(b))
        stack.push(frame(c))

        val snapshot = CycleCallSnapshot.instance(stack)
        val restored = with(snapshot) { UniqueInitializerCallStack.restore() }

        assertEquals(listOf(frame(a), frame(b), frame(c)), restored.toList())
        assertFalse(restored.isEmpty())
        assertEquals(frame(c), restored.peek())
    }

    @Test
    fun restoreFromEmptySnapshotProducesEmptyStack() {
        val empty = UniqueInitializerCallStack.create()
        val snapshot = CycleCallSnapshot.instance(empty)
        val restored = with(snapshot) { UniqueInitializerCallStack.restore() }
        assertTrue(restored.isEmpty())
    }
}
