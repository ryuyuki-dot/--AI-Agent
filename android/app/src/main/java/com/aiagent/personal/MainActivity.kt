package com.aiagent.personal

import android.Manifest
import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aiagent.personal.ui.LearningScreen
import com.aiagent.personal.ui.RecommendationScreen
import com.aiagent.personal.ui.WeekendPlanScreen
import com.aiagent.personal.worker.UsageStatsWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()
        scheduleUsageStatsWorker()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(
                        onOpenNotificationAccessSettings = {
                            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        },
                        onOpenUsageAccessSettings = {
                            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        },
                        hasNotificationAccess = hasNotificationListenerAccess(),
                        hasUsageAccess = hasUsageStatsAccess()
                    )
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun scheduleUsageStatsWorker() {
        val request = PeriodicWorkRequestBuilder<UsageStatsWorker>(6, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueue(request)
    }

    private fun hasNotificationListenerAccess(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabled?.contains(packageName) == true
    }

    private fun hasUsageStatsAccess(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}

sealed class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Weekend : Tab("weekend", "週末プラン", Icons.Filled.DateRange)
    object Actions : Tab("actions", "おすすめ", Icons.Filled.Star)
    object Learning : Tab("learning", "学習", Icons.Filled.School)
    object Settings : Tab("settings", "設定", Icons.Filled.Settings)
}

@Composable
fun AppRoot(
    onOpenNotificationAccessSettings: () -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    hasNotificationAccess: Boolean,
    hasUsageAccess: Boolean
) {
    val navController: NavHostController = rememberNavController()
    val tabs = listOf(Tab.Weekend, Tab.Actions, Tab.Learning, Tab.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Weekend.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Tab.Weekend.route) { WeekendPlanScreen() }
            composable(Tab.Actions.route) { RecommendationScreen() }
            composable(Tab.Learning.route) { LearningScreen() }
            composable(Tab.Settings.route) {
                com.aiagent.personal.ui.SettingsScreen(
                    hasNotificationAccess = hasNotificationAccess,
                    hasUsageAccess = hasUsageAccess,
                    onOpenNotificationAccessSettings = onOpenNotificationAccessSettings,
                    onOpenUsageAccessSettings = onOpenUsageAccessSettings
                )
            }
        }
    }
}
