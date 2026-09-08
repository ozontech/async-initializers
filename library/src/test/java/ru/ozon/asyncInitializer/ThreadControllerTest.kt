package ru.ozon.asyncInitializer

import ru.ozon.asyncInitializer.library.exceptions.AlreadyOnMainThreadException
import ru.ozon.asyncInitializer.library.threads.MainOptimizedRunner
import ru.ozon.asyncInitializer.library.threads.ThreadController
import ru.ozon.asyncInitializer.util.DefaultTestPlatformMainThread
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ThreadControllerTest {

    // Текущий тестовый поток считается main
    private fun mainController(): ThreadController {
        val platform = DefaultTestPlatformMainThread()
        return ThreadController(MainOptimizedRunner(platform))
    }

    @Test
    fun dispatchOnMainWithBlockingThrowsWhenAlreadyOnMain() {
        val controller = mainController()
        assertFailsWith<AlreadyOnMainThreadException> {
            controller.dispatchOnMainWithBlocking { }
        }
    }

    @Test
    fun dispatchOnMainFromWorkerRunsAction() {
        val platform = DefaultTestPlatformMainThread()
        val controller = ThreadController(MainOptimizedRunner(platform))

        val started = CountDownLatch(1)
        val finished = CountDownLatch(1)
        val actionRan = AtomicBoolean(false)

        val worker = thread {
            started.countDown()
            controller.dispatchOnMainWithBlocking {
                actionRan.set(true)
            }
            finished.countDown()
        }
        started.await()

        // Пока worker заблокирован на future.get(), "main-поток" (тестовый) исполняет action
        while (finished.count > 0) {
            platform.actions.forEach { it.run() }
        }
        worker.join()
        assertTrue(actionRan.get())
    }

    @Test
    fun interruptedThreadPassesInterruptedExceptionUnwrapped() {
        val controller = mainController()
        val started = CountDownLatch(1)
        val caught = AtomicReference<Throwable?>(null)

        val worker = thread {
            started.countDown()
            try {
                controller.dispatchOnMainWithBlocking { }
            } catch (e: Throwable) {
                caught.set(e)
            }
        }
        started.await()
        worker.interrupt()
        worker.join()

        val throwable = caught.get()
        assertTrue(throwable is InterruptedException)
        assertEquals(null, throwable.cause)
    }

}
