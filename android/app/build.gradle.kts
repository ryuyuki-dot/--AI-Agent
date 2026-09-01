plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.aiagent.personal"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aiagent.personal"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        // ↓バックエンドのURLに合わせて変更してください（例: Cloud Run発行URL）
           buildConfigField("String", "API_BASE_URL", "\"https://personal-ai-agent-api-37jq.onrender.com/\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // Icons.Filled.School / Refresh 等、coreセットに含まれないアイコンのために必要
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
