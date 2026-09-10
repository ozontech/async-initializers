package ru.ozon.asyncInitializer.demo.plugin.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.ComponentKeys

@Immutable
data class MainState(
    val initializers: PersistentMap<ComponentKeys, InitializerState>,
    val wasStarted: Boolean,
    val spendTime: Long?
)

/**
 * Live status of initializers for demonstrating which initializers are currently initializing
 */
enum class InitializerState {

    IDLE,
    WAITING,
    RUNNING,
    FINISHED,
}

object InitializerStatus {

    /**
     * Initializer display order (matches the mixed graph)
     */
    private fun defaultState(): PersistentMap<ComponentKeys, InitializerState> {
        return persistentHashMapOf<ComponentKeys, InitializerState>().mutate { map ->
            ComponentKeys.entries.forEach { map[it] = InitializerState.IDLE }
        }
    }


    private val _state = MutableStateFlow(
        MainState(
            initializers = defaultState(),
            wasStarted = false,
            spendTime = null
        )
    )

    val state: StateFlow<MainState> get() = _state.asStateFlow()

    val wasStarted: Boolean get() = state.value.wasStarted

    private var startTime: Long? = null
    fun run() {
        startTime = System.currentTimeMillis()
        _state.update { state ->
            val initializers = state.initializers
            state.copy(
                initializers = initializers.mutate {
                    val keys = it.keys.toSet()
                    keys.forEach { key -> it[key] = InitializerState.WAITING }
                },
                wasStarted = true
            )
        }
    }

    fun running(name: ComponentKeys) {
        _state.update { state ->
            val initializers = state.initializers
            state.copy(initializers = initializers.mutate { it[name] = InitializerState.RUNNING })
        }
    }

    fun finished(name: ComponentKeys) {
        _state.update { state ->
            val initializers = state.initializers.mutate { it[name] = InitializerState.FINISHED }
            val fullFinished = initializers.all { (_, value) -> value == InitializerState.FINISHED }
            state.copy(
                initializers = initializers,
                spendTime = if (fullFinished) System.currentTimeMillis() - checkNotNull(startTime) else null
            )
        }
    }
}