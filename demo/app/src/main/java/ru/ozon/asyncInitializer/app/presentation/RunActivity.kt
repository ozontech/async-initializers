@file:OptIn(ExperimentalMaterial3Api::class)
package ru.ozon.asyncInitializer.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.privatik.jankCanary.ui.theme.DemoLibraryTheme
import ru.ozon.asyncInitializer.app.data.InitializersRepositoryImpl
import ru.ozon.asyncInitializer.app.data.PreloadedInitializersDataSource

class RunActivity : ComponentActivity() {

    private val vm: RunViewModel by viewModels(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dataStore = PreloadedInitializersDataSource(application)
                    return RunViewModel(
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
                val state by vm.state.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("Run Tests") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                    )
                                }
                            },
                        )
                    },
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        RunContent(
                            state = state,
                            onStart = { vm.runAll() },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RunContent(
    state: RunState,
    onStart: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onStart,
            enabled = !state.isRunning,
            modifier = Modifier.weight(1f),
        ) {
            Text(if (state.isRunning) "Running..." else "Start Tests")
        }
    }

    HorizontalDivider()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        // Sync column
        TestColumn(
            title = "Синхронный запуск",
            tests = state.syncTests,
            medianMs = state.syncMedianMs,
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp),
        )

        // Divider between columns
        VerticalDivider()

        // Async column
        TestColumn(
            title = "Асинхронный запуск",
            tests = state.asyncTests,
            medianMs = state.asyncMedianMs,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        )
    }
}

@Composable
private fun TestColumn(
    title: String,
    tests: List<TestRun>,
    medianMs: Long?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            itemsIndexed(tests) { index, test ->
                TestRow(index = index, test = test)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Median time
        Text(
            text = if (medianMs != null) {
                "Медианное: ${medianMs}ms"
            } else {
                "Медианное: —"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
        )
    }
}

@Composable
private fun TestRow(index: Int, test: TestRun) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Тест ${index + 1}",
            style = MaterialTheme.typography.bodySmall,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            when (test.status) {
                TestStatus.NOT_STARTED -> {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Ожидание",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 11.sp,
                    )
                }
                TestStatus.IN_PROGRESS -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Выполнение",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                    )
                }
                TestStatus.FINISHED -> {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)),
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "${test.elapsedMs}ms",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}
