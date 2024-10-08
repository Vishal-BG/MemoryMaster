package com.memorymaster

import android.content.Context
import com.memorymaster.MemoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemoryLeakDetector(private val context: Context) {
    private val memorySnapshots = mutableMapOf<String, List<Long>>()

    suspend fun detectMemoryLeaks(): List<AppMemoryLeak> = withContext(Dispatchers.Default) {
        val currentSnapshot = MemoryManager.getRecentlyUsedApps().associate { it.packageName to it.memoryUsage }

        val leaks = mutableListOf<AppMemoryLeak>()

        for ((packageName, currentUsage) in currentSnapshot) {
            val snapshots = memorySnapshots.getOrPut(packageName) { mutableListOf() }
            (snapshots as MutableList).add(currentUsage)

            if (snapshots.size > 10) {  // Keep last 10 snapshots
                snapshots.removeAt(0)
            }

            if (snapshots.size == 10) {
                val trend = calculateTrend(snapshots)
                if (trend > 0.05) {  // If memory usage is consistently increasing by more than 5%
                    leaks.add(AppMemoryLeak(packageName, trend))
                }
            }
        }

        leaks
    }

    private fun calculateTrend(snapshots: List<Long>): Double {
        val n = snapshots.size
        val sumX = (0 until n).sum().toDouble()
        val sumY = snapshots.sum().toDouble()
        val sumXY = snapshots.mapIndexed { index, value -> index * value }.sum().toDouble()
        val sumXX = (0 until n).sumOf { it * it }.toDouble()

        val slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX)
        return slope / snapshots.average()  // Normalize by average memory usage
    }
}

data class AppMemoryLeak(
    val packageName: String,
    val leakTrend: Double
)