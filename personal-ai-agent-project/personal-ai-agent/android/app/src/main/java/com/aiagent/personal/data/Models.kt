package com.aiagent.personal.data

// ===== Events / SNS通知 =====
data class ParseNotificationRequest(
    val source_app: String,
    val sender: String,
    val raw_text: String,
    val received_at: String
)

data class DetectedEventDetail(
    val event_id: String,
    val title: String,
    val start_datetime: String,
    val end_datetime: String,
    val location: String?,
    val has_conflict: Boolean,
    val conflicting_event_title: String?
)

data class ParseNotificationResponse(
    val is_schedule_intent: Boolean,
    val detected_event: DetectedEventDetail?
)

data class ApproveEventResponse(
    val event_id: String,
    val status: String,
    val created_schedule_id: String,
    val message: String
)

// ===== Weekend Planner =====
data class FreeSlot(val start_time: String, val end_time: String)

data class WeekendPlanRequest(
    val target_weekend_date: String,
    val free_slots: List<FreeSlot>
)

data class ScheduleSuggestion(
    val title: String,
    val start_datetime: String,
    val end_datetime: String,
    val category: String,
    val reason: String,
    val deep_link_url: String?
)

data class WeekendPlanResponse(
    val plan_id: String,
    val target_date: String,
    val suggestions: List<ScheduleSuggestion>
)

// ===== Learning =====
data class LearningPlanRequest(
    val topic: String,
    val desired_duration_days: Int = 5
)

data class EvidenceSource(val source_name: String, val url: String, val snippet: String)

data class LearningModule(
    val step_number: Int,
    val module_id: String,
    val title: String,
    val content_markdown: String,
    val evidences: List<EvidenceSource>
)

data class LearningPlanResponse(
    val plan_id: String,
    val topic: String,
    val total_steps: Int,
    val modules: List<LearningModule>
)

// ===== Actions / Usage =====
data class AppUsageEntry(
    val package_name: String,
    val app_category: String?,
    val duration_seconds: Int,
    val last_opened_at: String
)

data class UsageLogRequest(val entries: List<AppUsageEntry>)
data class UsageLogResponse(val received: Int, val message: String)

data class ActionRecommendation(
    val recommendation_id: String,
    val action_type: String,
    val title: String,
    val description: String,
    val reason: String,
    val deep_link_url: String?
)

data class ActionRecommendationListResponse(val recommendations: List<ActionRecommendation>)
