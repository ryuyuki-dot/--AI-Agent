package com.aiagent.personal.worker

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aiagent.personal.data.AppUsageEntry
import com.aiagent.personal.data.UsageLogRequest
import com.aiagent.personal.network.ApiClient
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * UsageStatsManager経由でアプリ利用状況を収集し、バックエンドに送信する定期ジョブ。
 * 事前に「使用状況へのアクセス」権限（設定 > アプリ > 特別なアプリアクセス）が必要。
 */
class UsageStatsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val usageManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val end = System.currentTimeMillis()
            val start = end - TimeUnit.HOURS.toMillis(6)

            val stats = usageManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, start, end)
            val entries = stats
                .filter { it.totalTimeInForeground > 0 }
                .map {
                    AppUsageEntry(
                        package_name = it.packageName,
                        app_category = null,
                        duration_seconds = (it.totalTimeInForeground / 1000).toInt(),
                        last_opened_at = Instant.ofEpochMilli(it.lastTimeUsed).toString()
                    )
                }

            if (entries.isNotEmpty()) {
                ApiClient.service.sendUsageLogs(UsageLogRequest(entries))
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
