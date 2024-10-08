package com.memorymaster

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class AdaptiveMemoryAllocator(private val context: Context) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val appUsageHistory = mutableMapOf<String, List<UsageStats>>()

    suspend fun learnUsagePatterns() = withContext(Dispatchers.Default) {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 7 * 24 * 60 * 60 * 1000L // Last 7 days

        val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        // Group usage stats by package name
        appUsageHistory.clear()
        for (usageStats in usageStatsList) {
            val list = appUsageHistory.getOrPut(usageStats.packageName) { mutableListOf() }
            (list as MutableList).add(usageStats)
        }
    }

    suspend fun predictMemoryAllocation(): Map<String, Long> = withContext(Dispatchers.Default) {
        val currentTime = Calendar.getInstance()
        val dayOfWeek = currentTime.get(Calendar.DAY_OF_WEEK)
        val hourOfDay = currentTime.get(Calendar.HOUR_OF_DAY)

        val predictions = mutableMapOf<String, Long>()

        for ((packageName, usageStatsList) in appUsageHistory) {
            val averageUsage = usageStatsList
                .filter {
                    val usageTime = Calendar.getInstance().apply { timeInMillis = it.lastTimeUsed }
                    usageTime.get(Calendar.DAY_OF_WEEK) == dayOfWeek &&
                            usageTime.get(Calendar.HOUR_OF_DAY) == hourOfDay
                }
                .map { it.totalTimeInForeground }
                .average()

            if (averageUsage > 0) {
                // Allocate memory based on average usage time
                // This is a simplistic approach and should be refined based on actual app behavior
                val allocatedMemory = (averageUsage / 3600000 * 100 * 1024 * 1024).toLong() // 100MB per hour of usage
                predictions[packageName] = allocatedMemory
            }
        }

        predictions
    }
}