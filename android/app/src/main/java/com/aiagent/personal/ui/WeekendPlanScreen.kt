package com.aiagent.personal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiagent.personal.data.FreeSlot
import com.aiagent.personal.data.ScheduleSuggestion
import com.aiagent.personal.data.WeekendPlanRequest
import com.aiagent.personal.network.ApiClient
import kotlinx.coroutines.launch

/**
 * 実運用では CalendarContract から空き時間を計算しますが、
 * ここでは分かりやすさのためサンプルの空き時間を使ってバックエンドに提案をリクエストします。
 */
@Composable
fun WeekendPlanScreen() {
    var suggestions by remember { mutableStateOf<List<ScheduleSuggestion>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadPlan() {
        loading = true
        error = null
        scope.launch {
            try {
                val response = ApiClient.service.createWeekendPlan(
                    WeekendPlanRequest(
                        target_weekend_date = "2026-09-05",
                        free_slots = listOf(
                            FreeSlot("2026-09-05T09:00:00+09:00", "2026-09-05T13:00:00+09:00"),
                            FreeSlot("2026-09-05T15:00:00+09:00", "2026-09-05T20:00:00+09:00")
                        )
                    )
                )
                suggestions = response.suggestions
            } catch (e: Exception) {
                error = "取得に失敗しました。バックエンドURLの設定を確認してください。"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadPlan() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("週末の提案", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            IconButton(onClick = { loadPlan() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "再取得")
            }
        }
        Spacer(Modifier.height(8.dp))

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(suggestions) { s -> SuggestionCard(s) }
        }
    }
}

@Composable
private fun SuggestionCard(s: ScheduleSuggestion) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(s.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("${s.start_datetime} 〜 ${s.end_datetime}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Text(s.reason, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
