package com.aiagent.personal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiagent.personal.data.ChatMessage
import com.aiagent.personal.data.LearningModule
import com.aiagent.personal.data.LearningPlanRequest
import com.aiagent.personal.data.TutorChatRequest
import com.aiagent.personal.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun LearningScreen() {
    var topic by remember { mutableStateOf("") }
    var modules by remember { mutableStateOf<List<LearningModule>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
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
                error = null
                scope.launch {
                    try {
                        val res = ApiClient.service.createLearningPlan(LearningPlanRequest(topic = topic))
                        modules = res.modules
                    } catch (e: Exception) {
                        error = "学習プランの作成に失敗しました: ${e.javaClass.simpleName} - ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("学習シナリオを作成（Geminiが生成）")
        }

        Spacer(Modifier.height(12.dp))
        if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
            items(modules) { module -> ModuleCard(topic = topic, module = module) }
        }
    }
}

@Composable
private fun ModuleCard(topic: String, module: LearningModule) {
    var expanded by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var question by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun sendQuestion() {
        val q = question.trim()
        if (q.isBlank() || sending) return
        val newHistory = messages + ChatMessage(role = "user", content = q)
        messages = newHistory
        question = ""
        sending = true
        scope.launch {
            try {
                val res = ApiClient.service.tutorChat(
                    TutorChatRequest(
                        topic = topic,
                        module_title = module.title,
                        module_content = module.content_markdown,
                        question = q,
                        history = newHistory
                    )
                )
                messages = newHistory + ChatMessage(role = "assistant", content = res.answer)
            } catch (e: Exception) {
                messages = newHistory + ChatMessage(role = "assistant", content = "（エラー: 回答の取得に失敗しました）")
            } finally {
                sending = false
            }
        }
    }

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

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "先生に質問するのを閉じる" else "この内容について先生に質問する")
            }

            if (expanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    messages.forEach { msg -> ChatBubble(msg) }
                    if (sending) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = question,
                            onValueChange = { question = it },
                            placeholder = { Text("質問を入力…") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { sendQuestion() }, enabled = !sending) {
                            Text("送信")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = msg.content,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
