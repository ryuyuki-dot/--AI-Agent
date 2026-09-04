package com.aiagent.personal.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * Geminiで生成した学習シナリオの履歴を、端末内（アプリのプライベート領域）にJSONとして保存する。
 * バックエンドにDBがまだ無いため、まずは端末内保存で「後から振り返れる」を実現する。
 */
data class SavedLearningPlan(
    val planId: String,
    val topic: String,
    val createdAtEpochMillis: Long,
    val modules: List<LearningModule>
)

object LearningHistoryStore {
    private const val FILE_NAME = "learning_history.json"
    private const val MAX_ENTRIES = 30
    private val gson = Gson()

    fun load(context: Context): List<SavedLearningPlan> {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) return emptyList()
            val json = file.readText()
            val type = object : TypeToken<List<SavedLearningPlan>>() {}.type
            gson.fromJson<List<SavedLearningPlan>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, plan: SavedLearningPlan): List<SavedLearningPlan> {
        val updated = (listOf(plan) + load(context)).take(MAX_ENTRIES)
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(gson.toJson(updated))
        return updated
    }

    fun delete(context: Context, planId: String): List<SavedLearningPlan> {
        val updated = load(context).filter { it.planId != planId }
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(gson.toJson(updated))
        return updated
    }
}
