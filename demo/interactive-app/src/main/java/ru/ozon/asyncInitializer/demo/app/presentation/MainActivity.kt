@file:OptIn(ExperimentalMaterial3Api::class)

package ru.ozon.asyncInitializer.demo.app.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.ozon.asyncInitializer.demo.app.data.InitializersRepositoryImpl
import ru.ozon.asyncInitializer.demo.app.data.PreloadedInitializersDataSource
import ru.ozon.asyncInitializer.demo.app.domain.models.Initializer
import ru.ozon.asyncInitializer.demo.app.domain.models.InitializerConfig
import ru.ozon.asyncInitializer.demo.app.presentation.theme.DemoLibraryTheme

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dataStore = PreloadedInitializersDataSource(application)
                    return MainViewModel(
                        initializersRepository = InitializersRepositoryImpl(
                            dataStore
                        )
                    ) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DemoLibraryTheme {
                val mainState = vm.state.collectAsState()

                InitializerDemoApp(
                    mainState = mainState,
                    onAddInitializer = { initializer -> vm.addInitializer(initializer) },
                    onRemoveInitializer = { initializer -> vm.removeInitializer(initializer) },
                    onSelectConfig = { config -> vm.selectPreload(config) },
                    onRemoveAll = { vm.removeAll() },
                    onShowCreateInitializerDialog = { vm.showCreateInitializerDialog() },
                    onShowSelectConfigDialog = { vm.showSelectConfigDialog() },
                    onShowRemoveAllDialog = { vm.showDeleteAllDialog() },
                    onHideDialogs = { vm.hideDialogs() },
                    onRun = {
                        if (vm.saveInitializers()) {
                            startActivity(Intent(this, RunActivity::class.java))
                        }
                     },
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InitializerDemoApp(
    mainState: State<MainState>,
    onAddInitializer: (Initializer) -> Unit,
    onRemoveInitializer: (Initializer) -> Unit,
    onSelectConfig: (InitializerConfig) -> Unit,
    onRemoveAll: () -> Unit,
    onShowCreateInitializerDialog: () -> Unit,
    onShowSelectConfigDialog: () -> Unit,
    onShowRemoveAllDialog: () -> Unit,
    onHideDialogs: () -> Unit,
    onRun: () -> Unit,
) {

    val hasInitializers = remember { derivedStateOf { mainState.value.initializers.isNotEmpty() } }
    val hasPreloadInitializerConfigs = remember { derivedStateOf { mainState.value.preloadInitializerConfigs.isNotEmpty() } }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Component Initializer Demo") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onShowCreateInitializerDialog) {
                Icon(Icons.Default.Add, contentDescription = "Create Initializer")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onRun,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Run")
                }

                Button(
                    onClick = onShowSelectConfigDialog,
                    enabled = hasPreloadInitializerConfigs.value,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Load Preset")
                }

                Button(
                    onClick = onShowRemoveAllDialog,
                    enabled = hasInitializers.value,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Delete All")
                }
            }

            HorizontalDivider()

            if (!hasInitializers.value) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No initializers yet.\nTap + to create one or load a preset.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val initializers by remember { derivedStateOf { mainState.value.initializers } }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = initializers,
                        key = { it.name }
                    ) { initializer ->

                        val dependencyNames = remember {
                            derivedStateOf {
                                mainState.value.getNamesForAllDependencies(initializer)
                            }
                        }

                        InitializerCard(
                            initializer = initializer,
                            dependencyNames = dependencyNames,
                            onDelete = onRemoveInitializer,
                        )
                    }
                }
            }
        }
    }

    ShowDialogs(
        mainState = mainState,
        onAddInitializer = onAddInitializer,
        onSelectConfig = onSelectConfig,
        onRemoveAll = onRemoveAll,
        onHideDialogs = onHideDialogs
    )
}

@Composable
private fun ShowDialogs(
    mainState: State<MainState>,
    onAddInitializer: (Initializer) -> Unit,
    onSelectConfig: (InitializerConfig) -> Unit,
    onRemoveAll: () -> Unit,
    onHideDialogs: () -> Unit
){
    val dialogState by remember { derivedStateOf { mainState.value.dialogState } }
    when (dialogState) {
        MainState.DialogShowState.SELECT_CONFIG -> {
            LoadConfigDialog(
                configs = remember { derivedStateOf { mainState.value.preloadInitializerConfigs } }.value,
                onDismiss = onHideDialogs,
                onSelect = { config -> onSelectConfig(config) },
            )
        }
        MainState.DialogShowState.CREATE_INITIALIZER -> {
            CreateInitializerDialog(
                existingInitializers = remember { derivedStateOf { mainState.value.initializers } }.value,
                onDismiss = onHideDialogs,
                onSave = { initializer -> onAddInitializer(initializer) },
            )
        }
        MainState.DialogShowState.DELETE_ALL -> {
            RemoveAllDialog(
                onDismiss = onHideDialogs,
                onRemoveAll = onRemoveAll
            )
        }
        MainState.DialogShowState.NONE -> Unit
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InitializerCard(
    initializer: Initializer,
    dependencyNames: State<List<String>>,
    onDelete: (Initializer) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = initializer.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (initializer.runOnMainThread) "Main thread" else "Background",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (initializer.runOnMainThread) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                IconButton(onClick = { onDelete(initializer) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete ${initializer.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Blocking time: ${initializer.blockingTimeMs}ms",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (dependencyNames.value.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dependencies:",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    dependencyNames.value.forEach { dep ->
                        AssistChip(
                            onClick = { /* no-op for display */ },
                            label = {
                                Text(
                                    text = dep,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateInitializerDialog(
    existingInitializers: List<Initializer>,
    onDismiss: () -> Unit,
    onSave: (Initializer) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var blockingTimeMs by remember { mutableStateOf("1000") }
    var runOnMainThread by remember { mutableStateOf(false) }
    var selectedDependencyIds by remember { mutableStateOf(setOf<String>()) }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Initializer") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Name") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Name cannot be empty") }
                    } else {
                        null
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = blockingTimeMs,
                    onValueChange = { blockingTimeMs = it },
                    label = { Text("Blocking time (ms)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Run on Main Thread")
                    Switch(
                        checked = runOnMainThread,
                        onCheckedChange = { runOnMainThread = it },
                    )
                }

                if (existingInitializers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    var expanded by remember { mutableStateOf(false) }
                    val sortedInitializers = remember(existingInitializers) {
                        existingInitializers.sortedBy { it.name }
                    }
                    val selectedCount = selectedDependencyIds.size

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                    ) {
                        OutlinedTextField(
                            value = if (selectedCount == 0) "" else "$selectedCount selected",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Dependencies") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            sortedInitializers.forEach { initializer ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedDependencyIds = if (initializer.id in selectedDependencyIds) {
                                            selectedDependencyIds - initializer.id
                                        } else {
                                            selectedDependencyIds + initializer.id
                                        }
                                    },
                                    leadingIcon = {
                                        Checkbox(
                                            checked = initializer.id in selectedDependencyIds,
                                            onCheckedChange = null,
                                        )
                                    },
                                    text = { Text(initializer.name) },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@TextButton
                    }
                    val time = blockingTimeMs.toLongOrNull() ?: 1000L
                    onSave(
                        Initializer(
                            name = name,
                            blockingTimeMs = time,
                            runOnMainThread = runOnMainThread,
                            dependenciesIds = selectedDependencyIds,
                        ),
                    )
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun RemoveAllDialog(
    onDismiss: () -> Unit,
    onRemoveAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete All") },
        text = { Text("Are you sure you want to delete all initializers?") },
        confirmButton = {
            TextButton(onClick = onRemoveAll) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun LoadConfigDialog(
    configs: List<InitializerConfig>,
    onDismiss: () -> Unit,
    onSelect: (InitializerConfig) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Preset") },
        text = {
            Column {
                configs.forEach { config ->
                    TextButton(
                        onClick = { onSelect(config) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = config.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${config.initializers.size} initializers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
