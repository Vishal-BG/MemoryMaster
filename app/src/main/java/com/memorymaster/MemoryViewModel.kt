package com.memorymaster

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.memorymaster.MemoryManager.balanceMemoryBetweenApps
import com.memorymaster.MemoryManager.clearAppCaches
import com.memorymaster.MemoryManager.compressInactiveData
import com.memorymaster.MemoryManager.defragmentMemory
import com.memorymaster.MemoryManager.getDetailedMemoryInfo
import com.memorymaster.MemoryManager.getRecentlyUsedApps
import com.memorymaster.MemoryManager.killBackgroundProcesses
import com.memorymaster.MemoryManager.optimizeSystemServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MemoryViewModel(application: Application) : AndroidViewModel(application) {
    private val adaptiveMemoryAllocator = AdaptiveMemoryAllocator(application)
    private val memoryLeakDetector = MemoryLeakDetector(application)

    private val _detailedMemoryInfo = MutableLiveData<DetailedMemoryInfo>()
    val detailedMemoryInfo: LiveData<DetailedMemoryInfo> = _detailedMemoryInfo

    private val _recentlyUsedApps = MutableLiveData<List<AppInfo>>()
    val recentlyUsedApps: LiveData<List<AppInfo>> = _recentlyUsedApps

    private val _memoryLeaks = MutableLiveData<List<AppMemoryLeak>>()
    val memoryLeaks: LiveData<List<AppMemoryLeak>> = _memoryLeaks

    private val _predictedMemoryAllocations = MutableLiveData<Map<String, Long>>()
    val predictedMemoryAllocations: LiveData<Map<String, Long>> = _predictedMemoryAllocations

    private val _optimizationProgress = MutableLiveData<Int>()
    val optimizationProgress: LiveData<Int> = _optimizationProgress

    init {
        MemoryManager.init(application)
    }

    fun startMemoryMonitoring() {
        viewModelScope.launch {
            while (true) {
                updateMemoryInfo()
                delay(5000) // Update every 5 seconds
            }
        }
    }

    private suspend fun updateMemoryInfo() {
        withContext(Dispatchers.Default) {
            _detailedMemoryInfo.postValue(MemoryManager.getDetailedMemoryInfo())
            _recentlyUsedApps.postValue(MemoryManager.getRecentlyUsedApps())
            _memoryLeaks.postValue(memoryLeakDetector.detectMemoryLeaks())
            adaptiveMemoryAllocator.learnUsagePatterns()
            _predictedMemoryAllocations.postValue(adaptiveMemoryAllocator.predictMemoryAllocation())
        }
    }

    suspend fun optimizeMemory() = withContext(Dispatchers.Default) {
        val recentlyUsedApps = getRecentlyUsedApps()
        val totalMemory = getDetailedMemoryInfo().totalMem
        val availableMemory = getDetailedMemoryInfo().availMem

        // Step 1: Close background apps
        if (availableMemory < totalMemory * 0.2) { // If less than 20% memory available
            recentlyUsedApps
                .filter { it.usageTime < 60000 || it.memoryUsage > 200_000_000 } // Close apps used less than 1 minute or using more than 200MB
                .forEach { app ->
                    killBackgroundProcesses(app.packageName)
                }
        }

        // Step 2: Clear app caches if memory is still low
        if (getDetailedMemoryInfo().availMem < totalMemory * 0.3) {
            clearAppCaches()
        }

        // Step 3: Compress inactive data
        compressInactiveData()

        // Step 4: Balance memory between apps
        balanceMemoryBetweenApps()

        // Step 5: Defragment memory
        defragmentMemory()

        // Step 6: Force garbage collection
        System.gc()
        Runtime.getRuntime().gc()

        // Step 7: Optimize system services
        optimizeSystemServices()

        // Log to confirm RAM freeing
        println("Memory optimization complete. Available memory: ${getDetailedMemoryInfo().availMem}")

        // Step 8: Explicitly free unused objects
        System.gc()  // Suggest garbage collection
        Runtime.getRuntime().gc()  // Ensure garbage collection is triggered

        // Step 9: Recheck memory info after optimization
        val memoryAfterOptimization = getDetailedMemoryInfo()
        println("Memory after optimization: ${memoryAfterOptimization.availMem} / ${memoryAfterOptimization.totalMem}")
    }


    fun prioritizeForegroundApp(packageName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                MemoryManager.prioritizeForegroundApp(packageName)
                MemoryManager.allocateExtraMemoryForApp(packageName)
                updateMemoryInfo() // Refresh data after prioritization
            }
        }
    }

    fun analyzeMemoryUsage(): LiveData<MemoryAnalysisResult> {
        val result = MutableLiveData<MemoryAnalysisResult>()
        viewModelScope.launch {
            val analysis = withContext(Dispatchers.Default) {
                MemoryManager.performDetailedMemoryAnalysis()
            }
            result.postValue(analysis)
        }
        return result
    }

    fun scheduleMemoryOptimization(intervalHours: Int) {
        MemoryManager.schedulePeriodicOptimization(intervalHours)
    }
}















