package com.aiagent.personal.network

import com.aiagent.personal.data.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // --- Events / SNS通知 ---
    @POST("api/v1/events/parse-notification")
    suspend fun parseNotification(@Body req: ParseNotificationRequest): ParseNotificationResponse

    @POST("api/v1/events/{eventId}/approve")
    suspend fun approveEvent(@Path("eventId") eventId: String): ApproveEventResponse

    @POST("api/v1/events/{eventId}/reject")
    suspend fun rejectEvent(@Path("eventId") eventId: String)

    // --- Weekend Planner ---
    @POST("api/v1/schedules/weekend-plan")
    suspend fun createWeekendPlan(@Body req: WeekendPlanRequest): WeekendPlanResponse

    // --- Learning ---
    @POST("api/v1/learning/plans")
    suspend fun createLearningPlan(@Body req: LearningPlanRequest): LearningPlanResponse

    // --- Actions / Usage ---
    @POST("api/v1/actions/usage-logs")
    suspend fun sendUsageLogs(@Body req: UsageLogRequest): UsageLogResponse

    @GET("api/v1/actions/recommendations")
    suspend fun getRecommendations(): ActionRecommendationListResponse
}
