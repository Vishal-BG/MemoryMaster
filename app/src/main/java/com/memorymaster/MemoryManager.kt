package com.memorymaster

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Serializable

object MemoryManager {
    private lateinit var context: Context
    private lateinit var activityManager: ActivityManager
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var packageManager: PackageManager
    private lateinit var batteryManager: BatteryManager
    private lateinit var alarmManager: AlarmManager

    fun init(context: Context) {
        this.context = context.applicationContext
        activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        packageManager = context.packageManager
        batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    suspend fun getDetailedMemoryInfo(): DetailedMemoryInfo = withContext(Dispatchers.Default) {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val recentlyUsedApps = getRecentlyUsedApps()
        val appMemoryInfo = recentlyUsedApps.map { app ->
            AppMemoryInfo(
                packageName = app.packageName,
                appName = app.appName,
                memoryUsage = app.memoryUsage
            )
        }

        DetailedMemoryInfo(
            totalMem = memInfo.totalMem,
            availMem = memInfo.availMem,
            threshold = memInfo.threshold,
            lowMemory = memInfo.lowMemory,
            appMemoryInfo = appMemoryInfo
        )
    }

    suspend fun getRecentlyUsedApps(): List<AppInfo> = withContext(Dispatchers.Default) {
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 24 * 3600000 // Look back 24 hours

        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime)

        usageStats.mapNotNull { stats ->
            try {
                val packageName = stats.packageName
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()

                val memoryUsage = getAppMemoryUsage(packageName)
                val usageTime = stats.totalTimeInForeground
                val batteryConsumption = getBatteryConsumption(packageName)
                val permissions = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                    .requestedPermissions?.toList() ?: emptyList()

                AppInfo(
                    packageName = packageName,
                    appName = appName,
                    memoryUsage = memoryUsage,
                    usageTime = usageTime,
                    batteryConsumption = batteryConsumption,
                    permissions = permissions
                )
            } catch (e: PackageManager.NameNotFoundException) {
                null // Skip if package is not found
            }
        }.sortedByDescending { it.usageTime }
    }

    private fun getAppMemoryUsage(packageName: String): Long {
        val processes = activityManager.runningAppProcesses ?: return 0
        val uid = try {
            packageManager.getApplicationInfo(packageName, 0).uid
        } catch (e: PackageManager.NameNotFoundException) {
            return 0
        }

        return processes
            .filter { it.uid == uid }
            .sumOf { process ->
                val pids = intArrayOf(process.pid)
                val memoryInfo = activityManager.getProcessMemoryInfo(pids)
                memoryInfo[0].totalPss * 1024L // Convert to bytes
            }
    }

    private fun getBatteryConsumption(packageName: String): Float {
        // This is a placeholder implementation. In a real app, you'd need to implement
        // a way to track battery consumption per app, which is not directly provided by Android.
        // You might need to use a custom solution or third-party library for this.
        return 0f
    }

    suspend fun optimizeMemory() = withContext(Dispatchers.Default) {
        val recentlyUsedApps = getRecentlyUsedApps()
        val totalMemory = getDetailedMemoryInfo().totalMem
        val availableMemory = getDetailedMemoryInfo().availMem

        if (availableMemory < totalMemory * 0.2) { // If less than 20% memory available
            recentlyUsedApps
                .filter { it.usageTime < 60000 || it.memoryUsage > 200_000_000 } // Close apps used less than 1 minute or using more than 200MB
                .forEach { app ->
                    killBackgroundProcesses(app.packageName)
                }
        }

        // Clear app caches if memory is still low
        if (getDetailedMemoryInfo().availMem < totalMemory * 0.3) {
            clearAppCaches()
        }

        compressInactiveData()
        balanceMemoryBetweenApps()
        defragmentMemory()
        optimizeSystemServices()
    }

    fun killBackgroundProcesses(packageName: String) {
        activityManager.killBackgroundProcesses(packageName)
    }

    fun clearAppCaches() {
        val packages = packageManager.getInstalledPackages(0)
        for (packageInfo in packages) {
            val dir = context.cacheDir
            if (dir != null && dir.isDirectory) {
                dir.deleteRecursively()
            }
        }
    }

    fun compressInactiveData() {
        // Placeholder for data compression logic
        // This would involve identifying inactive data and applying compression algorithms
        println("Compressing inactive data")
    }

    suspend fun balanceMemoryBetweenApps() = withContext(Dispatchers.Default) {
        val apps = getRecentlyUsedApps()
        val totalAvailableMemory = getDetailedMemoryInfo().availMem
        val appCount = apps.size

        if (appCount > 0) {
            val averageMemoryPerApp = totalAvailableMemory / appCount
            apps.forEach { app ->
                if (app.memoryUsage > averageMemoryPerApp) {
                    // Reduce memory for apps using more than average
                    setAppMemoryLimit(app.packageName, averageMemoryPerApp)
                }
            }
        }
    }

    fun setAppMemoryLimit(packageName: String, limit: Long) {
        // Placeholder for setting app memory limits
        // This would involve using Android's memory management APIs to set limits for specific apps
        println("Setting memory limit of $limit bytes for $packageName")
    }

    fun defragmentMemory() {
        // Placeholder for memory defragmentation logic
        // This would involve reorganizing memory to reduce fragmentation
        println("Defragmenting memory")
    }

    fun optimizeSystemServices() {
        // Placeholder for system service optimization
        // This would involve analyzing and optimizing system-level services
        println("Optimizing system services")
    }

    fun prioritizeForegroundApp(packageName: String) {
        // Placeholder for prioritizing foreground app
        // This would involve adjusting process priorities and resource allocation
        println("Prioritizing foreground app: $packageName")
    }

    fun allocateExtraMemoryForApp(packageName: String) {
        // Placeholder for allocating extra memory to an app
        // This would involve adjusting memory limits for the specific app
        println("Allocating extra memory for app: $packageName")
    }

    suspend fun performDetailedMemoryAnalysis(): MemoryAnalysisResult = withContext(Dispatchers.Default) {
        val apps = getRecentlyUsedApps()
        val totalMemory = getDetailedMemoryInfo().totalMem
        val availableMemory = getDetailedMemoryInfo().availMem

        val memoryDistribution = apps.associateWith { it.memoryUsage }
        val largestConsumers = memoryDistribution.entries.sortedByDescending { it.value }.take(5)

        MemoryAnalysisResult(
            totalMemory = totalMemory,
            availableMemory = availableMemory,
            memoryDistribution = memoryDistribution,
            largestConsumers = largestConsumers.map { it.key.packageName to it.value }.toMap()
        )
    }

    fun schedulePeriodicOptimization(intervalHours: Int) {
        val intent = Intent(context, MemoryOptimizationReceiver::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + intervalHours * 3600 * 1000,
            (intervalHours * 3600 * 1000).toLong(),
            pendingIntent
        )
    }
}

data class DetailedMemoryInfo(
    val totalMem: Long,
    val availMem: Long,
    val threshold: Long,
    val lowMemory: Boolean,
    val appMemoryInfo: List<AppMemoryInfo>
)

data class AppMemoryInfo(
    val packageName: String,
    val appName: String,
    val memoryUsage: Long
)

data class AppInfo(
    val packageName: String,
    val appName: String,
    val memoryUsage: Long,
    val usageTime: Long,
    val batteryConsumption: Float,
    val permissions: List<String>
) : Serializable

data class MemoryAnalysisResult(
    val totalMemory: Long,
    val availableMemory: Long,
    val memoryDistribution: Map<AppInfo, Long>,
    val largestConsumers: Map<String, Long>
)