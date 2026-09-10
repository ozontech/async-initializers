package ru.ozon.asyncInitializer.demo.app.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.ozon.asyncInitializer.demo.app.domain.InitializersRepository
import ru.ozon.asyncInitializer.demo.app.domain.models.Initializer
import ru.ozon.asyncInitializer.demo.app.domain.models.InitializerConfig
import ru.ozon.asyncInitializer.demo.app.presentation.MainState.DialogShowState
import kotlin.collections.plus

@Immutable
internal data class MainState(
    val initializers: List<Initializer> = emptyList(),
    val preloadInitializerConfigs: List<InitializerConfig> = emptyList(),
    val dialogState: DialogShowState = DialogShowState.NONE
) {

    private val idToNameMap by lazy(LazyThreadSafetyMode.NONE) {
        initializers.associateBy(
            keySelector = { it.id },
            valueTransform = { it.name }
        )
    }

    fun getNamesForAllDependencies(initializer: Initializer): List<String> {
       return initializer.dependenciesIds.mapNotNull { id -> idToNameMap[id] }
    }

    enum class DialogShowState {
        SELECT_CONFIG,
        CREATE_INITIALIZER,
        DELETE_ALL,
        NONE
    }

}

internal class MainViewModel(
    private val initializersRepository: InitializersRepository
): ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state get() = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val configs = initializersRepository.loadPreloadConfigs()
            _state.update { currentState -> currentState.copy(preloadInitializerConfigs = configs) }
        }
    }

    fun removeAll() {
        _state.update { currentState ->
            currentState.copy(
                initializers = emptyList(),
                dialogState = DialogShowState.NONE
            )
        }
    }

    fun selectPreload(config: InitializerConfig) {
        _state.update { currentState ->
            currentState.copy(
                initializers = currentState.initializers + config.initializers,
                dialogState = DialogShowState.NONE
            )
        }
    }

    fun addInitializer(initializer: Initializer) {
        _state.update { currentState ->
            currentState.copy(
                initializers = currentState.initializers + initializer,
                dialogState = DialogShowState.NONE
            )
        }
    }

    fun showSelectConfigDialog() {
        _state.update { currentState -> currentState.copy(dialogState = DialogShowState.SELECT_CONFIG) }
    }

    fun showCreateInitializerDialog(){
        _state.update { currentState -> currentState.copy(dialogState = DialogShowState.CREATE_INITIALIZER) }
    }

    fun showDeleteAllDialog() {
        _state.update { currentState -> currentState.copy(dialogState = DialogShowState.DELETE_ALL) }
    }

    fun hideDialogs() {
        _state.update { currentState -> currentState.copy(dialogState = DialogShowState.NONE) }
    }

    fun removeInitializer(initializer: Initializer) {
        _state.update { currentState ->
            currentState.copy(initializers = currentState.initializers - initializer)
        }
    }

    fun saveInitializers(): Boolean {
        val initializers = state.value.initializers
        if (initializers.isNotEmpty()) {
            initializersRepository.saveInitializers(initializers)
            return true
        }
        return false
    }

}
