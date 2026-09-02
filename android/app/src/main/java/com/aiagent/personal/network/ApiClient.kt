package com.aiagent.personal.network

import com.aiagent.personal.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // build.gradle.kts の API_BASE_URL を実際のバックエンドURLに変更してください
    private val baseUrl: String = BuildConfig.API_BASE_URL

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Renderの無料プランはスリープからの復帰に最大50秒程度、
    // さらにGemini APIが混雑時に自動リトライで数秒〜十数秒かかることがあるため、
    // デフォルト(10秒)より大幅に長いタイムアウトを設定する。
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
