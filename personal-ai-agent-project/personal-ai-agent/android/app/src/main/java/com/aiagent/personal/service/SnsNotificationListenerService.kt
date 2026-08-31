package com.aiagent.personal.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.aiagent.personal.data.ParseNotificationRequest
import com.aiagent.personal.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

/**
 * SNSアプリ（LINE等）の通知を監視し、予定確認っぽい内容ならバックエンドに解析を依頼。
 * 予定意図が検出されたら、承認/却下ボタン付きの確認通知を表示する。
 */
class SnsNotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    // 監視対象アプリ（必要に応じて追加してください）
    private val monitoredPackages = mapOf(
        "jp.naver.line.android" to "LINE",
        "com.facebook.orca" to "Messenger",
        "com.instagram.android" to "Instagram"
    )

    companion object {
        const val CHANNEL_ID = "schedule_confirmation"
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_TITLE = "extra_title"
        const val ACTION_APPROVE = "com.aiagent.personal.ACTION_APPROVE"
        const val ACTION_REJECT = "com.aiagent.personal.ACTION_REJECT"
    }

    override fun onCreate() {
        super.onCreate()
        createChannelIfNeeded()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val appName = monitoredPackages[sbn.packageName] ?: return

        val extras = sbn.notification.extras
        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "不明"
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        if (text.isBlank()) return

        scope.launch {
            try {
                val response = ApiClient.service.parseNotification(
                    ParseNotificationRequest(
                        source_app = appName,
                        sender = sender,
                        raw_text = text,
                        received_at = OffsetDateTime.now().toString()
                    )
                )
                if (response.is_schedule_intent && response.detected_event != null) {
                    showConfirmationNotification(
                        eventId = response.detected_event.event_id,
                        title = response.detected_event.title,
                        start = response.detected_event.start_datetime,
                        location = response.detected_event.location
                    )
                }
            } catch (e: Exception) {
                // TODO: 通信失敗時のリトライ/ログ収集
            }
        }
    }

    private fun showConfirmationNotification(eventId: String, title: String, start: String, location: String?) {
        val approveIntent = Intent(this, EventApprovalReceiver::class.java).apply {
            action = ACTION_APPROVE
            putExtra(EXTRA_EVENT_ID, eventId)
            putExtra(EXTRA_TITLE, title)
        }
        val rejectIntent = Intent(this, EventApprovalReceiver::class.java).apply {
            action = ACTION_REJECT
            putExtra(EXTRA_EVENT_ID, eventId)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val approvePendingIntent = PendingIntent.getBroadcast(this, eventId.hashCode(), approveIntent, flags)
        val rejectPendingIntent = PendingIntent.getBroadcast(this, eventId.hashCode() + 1, rejectIntent, flags)

        val locationText = if (location != null) " @ $location" else ""

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("予定を検出しました")
            .setContentText("$title（$start$locationText）")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$title（$start$locationText）\nスケジュールに登録しますか？"))
            .addAction(0, "承認してスケジュール登録", approvePendingIntent)
            .addAction(0, "却下", rejectPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(eventId.hashCode(), notification)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "予定確認",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "SNSメッセージから検出した予定の承認確認"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
