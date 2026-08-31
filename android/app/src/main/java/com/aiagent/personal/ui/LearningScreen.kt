package com.aiagent.personal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiagent.personal.data.LearningModule
import com.aiagent.personal.data.LearningPlanRequest
import com.aiagent.personal.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun LearningScreen() {
    var topic by remember { mutableStateOf("") }
    var modules by remember { mutableStateOf<List<LearningModule>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("学びたいことをリクエスト", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = topic,
            onValueChange = { topic = it },
            label = { Text("例: Pythonによるデータ分析基礎") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (topic.isBlank()) return@Button
                loading = true
                scope.launch {
                    try {
                        val res = ApiClient.service.createLearningPlan(LearningPlanRequest(topic = topic))
                        modules = res.modules
                    } catch (_: Exception) {
                        // TODO: エラーハンドリング
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.align(androidx.compose.ui.Alignment.End)
        ) {
            Text("学習シナリオを作成")
        }

        Spacer(Modifier.height(12.dp))
        if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(modules) { module -> ModuleCard(module) }
        }
    }
}

@Composable
private fun ModuleCard(module: LearningModule) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Step ${module.step_number}: ${module.title}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(module.content_markdown, style = MaterialTheme.typography.bodyMedium)
            if (module.evidences.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("エビデンス", style = MaterialTheme.typography.labelLarge)
                module.evidences.forEach { ev ->
                    Text("・${ev.source_name}: ${ev.url}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
