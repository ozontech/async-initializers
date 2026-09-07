package ru.ozon.asyncInitializer.app.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import ru.ozon.asyncInitializer.app.domain.InitializersRepository
import ru.ozon.asyncInitializer.app.presentation.initializers.InitializerRunner

internal val TEST_COUNT = 10

internal enum class TestStatus {
    NOT_STARTED,
    IN_PROGRESS,
    FINISHED,
}

internal data class TestRun(
    val status: TestStatus = TestStatus.NOT_STARTED,
    val elapsedMs: Long? = null,
)

@Immutable
internal data class RunState(
    val syncTests: List<TestRun> = List(TEST_COUNT) { TestRun() },
    val asyncTests: List<TestRun> = List(TEST_COUNT) { TestRun() },
    val syncMedianMs: Long? = null,
    val asyncMedianMs: Long? = null,
    val isRunning: Boolean = false,
)



internal class RunViewModel(
    private val initializersRepository: InitializersRepository,
) : ViewModel() {

    private val runner = InitializerRunner()
    private var runnerJob: Job? = null

    private val _state = MutableStateFlow(RunState())
    val state get() = _state.asStateFlow()


    fun runAll() {
        val initializers = initializersRepository.getSavedInitializers()
        if (initializers.isEmpty()) return
        if (runnerJob?.isActive == true) return

        val usedIds = buildSet { initializers.forEach { add(it.id) } }
        val filteredInitializers = initializers.map { initializer ->
            initializer.copy(
                dependenciesIds = initializer.dependenciesIds.filter { id -> id in usedIds }.toSet()
            )
        }
        runnerJob = runner.run(
            testCount = TEST_COUNT,
            initializers = filteredInitializers
        )
            .onStart {
                setInitialState()
            }
            .onEach { status ->
                when (status) {
                    InitializerRunner.RunStatus.AlreadyRunning -> Unit
                    is InitializerRunner.RunStatus.Complete -> complete(status.syncMedianMs, status.asyncMedianMs)
                    InitializerRunner.RunStatus.Start -> start()
                    is InitializerRunner.RunStatus.UpdateTestStatus.Finish -> finishTest(status.index, status.isSync, status.spendTime)
                    is InitializerRunner.RunStatus.UpdateTestStatus.Running -> runTest(status.index, status.isSync)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun setInitialState() {
        _state.update { RunState() }
    }

    private fun start() {
        _state.update { currentState -> currentState.copy(isRunning = true) }
    }

    private fun complete(
        syncMedianMs: Long,
        asyncMedianMs: Long,
    ) {
        _state.update{ currentState ->
            currentState.copy(
                syncMedianMs = syncMedianMs,
                asyncMedianMs = asyncMedianMs,
                isRunning = false
            )
        }
    }

    private fun runTest(
        index: Int,
        isSync: Boolean
    ) {
        _state.update{ currentState ->
            val currentTest = if (isSync) currentState.syncTests else currentState.asyncTests
            val updatedTest = currentTest
                .toMutableList()
                .apply {
                    set(index, TestRun(status = TestStatus.IN_PROGRESS))
                }
                .toList()

            currentState.copy(
                syncTests = if (isSync) updatedTest else currentState.syncTests,
                asyncTests = if (!isSync) updatedTest else currentState.asyncTests,
            )
        }
    }

    private fun finishTest(
        index: Int,
        isSync: Boolean,
        spendTime: Long
    ) {
        _state.update{ currentState ->
            val currentTest = if (isSync) currentState.syncTests else currentState.asyncTests
            val updatedTest = currentTest
                .toMutableList()
                .apply {
                    set(index, TestRun(status = TestStatus.FINISHED, elapsedMs = spendTime))
                }
                .toList()

            currentState.copy(
                syncTests = if (isSync) updatedTest else currentState.syncTests,
                asyncTests = if (!isSync) updatedTest else currentState.asyncTests,
            )
        }
    }

}
