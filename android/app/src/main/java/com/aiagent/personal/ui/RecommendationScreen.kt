package com.aiagent.personal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiagent.personal.data.ActionRecommendation
import com.aiagent.personal.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun RecommendationScreen() {
    var items by remember { mutableStateOf<List<ActionRecommendation>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun load() {
        loading = true
        scope.launch {
            try {
                items = ApiClient.service.getRecommendations().recommendations
            } catch (_: Exception) {
                // TODO: エラーハンドリング
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("あなたへのおすすめAction", style = MaterialTheme.typography.headlineSmall)
        Text(
            "アプリ利用状況をもとに提案しています",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { rec -> RecommendationCard(rec) }
        }
    }
}

@Composable
private fun RecommendationCard(rec: ActionRecommendation) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(rec.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(rec.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text("理由: ${rec.reason}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
