package ru.ozon.asyncInitializer.demo.plugin.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.ozon.asyncInitializer.demo.plugin.presentation.componentInitializers.ComponentKeys
import ru.ozon.asyncInitializer.demo.plugin.presentation.theme.PluginDemoTheme

private const val GRID_COLUMNS = 5

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PluginDemoTheme {
                GraphScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GraphScreen() {
    val state = InitializerStatus.state.collectAsState()

    val isRunning by remember { derivedStateOf { state.value.wasStarted } }
    val components by remember { derivedStateOf { state.value.initializers } }
    val total = components.size
    val finishedCount by remember {
        derivedStateOf { components.values.count { it == InitializerState.FINISHED } }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Async Component Initializers") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Button(
                onClick = { GraphInitializerRunner.run() },
                enabled = !isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    if (isRunning) "Ran…" else "Run"
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Initialized $finishedCount/$total",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    LinearProgressIndicator(
                        progress = { if (total == 0) 0f else finishedCount.toFloat() / total },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    InitializerGrid(items = ComponentKeys.entries, statuses = components)
                }
            }
        }
    }
}

@Composable
private fun InitializerGrid(
    items: List<ComponentKeys>,
    statuses: Map<ComponentKeys, InitializerState>,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        items.chunked(GRID_COLUMNS).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { item ->
                    val state = statuses[item] ?: InitializerState.IDLE
                    val (borderColor, borderWidth) = when (state) {
                        InitializerState.IDLE -> Color(0xFF9E9E9E) to 1.dp
                        InitializerState.WAITING -> Color(0xFFF9A825) to 1.dp
                        InitializerState.RUNNING -> Color(0xFFF57C00) to 3.dp
                        InitializerState.FINISHED -> Color(0xFF2E7D32) to 2.dp
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(88.dp)
                            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = item.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = borderColor,
                        )
                    }
                }
            }
        }
    }
}