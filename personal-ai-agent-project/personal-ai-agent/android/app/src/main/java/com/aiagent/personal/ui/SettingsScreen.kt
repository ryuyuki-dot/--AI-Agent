package com.aiagent.personal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    hasNotificationAccess: Boolean,
    hasUsageAccess: Boolean,
    onOpenNotificationAccessSettings: () -> Unit,
    onOpenUsageAccessSettings: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("権限設定", style = MaterialTheme.typography.headlineSmall)
        Text(
            "各機能を使うには、以下の権限をONにしてください（初回のみ）",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(16.dp))

        PermissionRow(
            title = "通知へのアクセス",
            description = "SNSメッセージから予定を検出するために必要です",
            granted = hasNotificationAccess,
            onClick = onOpenNotificationAccessSettings
        )
        Spacer(Modifier.height(12.dp))
        PermissionRow(
            title = "使用状況へのアクセス",
            description = "おすすめActionの提案のために必要です",
            granted = hasUsageAccess,
            onClick = onOpenUsageAccessSettings
        )
    }
}

@Composable
private fun PermissionRow(title: String, description: String, granted: Boolean, onClick: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
            if (granted) {
                AssistChip(onClick = {}, label = { Text("許可済み") })
            } else {
                Button(onClick = onClick) { Text("設定する") }
            }
        }
    }
}
