package com.aiagent.personal.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.aiagent.personal.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 予定確認通知の「承認」「却下」ボタンのタップを処理する。
 * 承認時はバックエンドの approve API を呼び、スケジュールに登録する。
 */
class EventApprovalReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getStringExtra(SnsNotificationListenerService.EXTRA_EVENT_ID) ?: return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(eventId.hashCode())

        val scope = CoroutineScope(Dispatchers.IO)
        when (intent.action) {
            SnsNotificationListenerService.ACTION_APPROVE -> {
                scope.launch {
                    try {
                        ApiClient.service.approveEvent(eventId)
                        toast(context, "スケジュールに登録しました")
                    } catch (e: Exception) {
                        toast(context, "登録に失敗しました")
                    }
                }
            }
            SnsNotificationListenerService.ACTION_REJECT -> {
                scope.launch {
                    try {
                        ApiClient.service.rejectEvent(eventId)
                        toast(context, "却下しました")
                    } catch (e: Exception) {
                        // TODO: エラーハンドリング
                    }
                }
            }
        }
    }

    private suspend fun toast(context: Context, message: String) {
        kotlinx.coroutines.withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
