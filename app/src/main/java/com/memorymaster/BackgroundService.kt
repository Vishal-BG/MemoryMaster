package com.memorymaster

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.memorymaster.R
import kotlinx.coroutines.*

class BackgroundService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "MemoryMasterChannel"

    private lateinit var adaptiveMemoryAllocator: AdaptiveMemoryAllocator

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        MemoryManager.init(this)
        adaptiveMemoryAllocator = AdaptiveMemoryAllocator(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            while (isActive) {
                try {
                    manageBackgroundApps()
                    delay(5 * 60 * 1000) // Run every 5 minutes
                } catch (e: Exception) {
                    // Log the error
                    e.printStackTrace()
                }
            }
        }
        return START_STICKY
    }

    private suspend fun manageBackgroundApps() {
        adaptiveMemoryAllocator.learnUsagePatterns()
        val predictedAllocations = adaptiveMemoryAllocator.predictMemoryAllocation()
        val runningApps = MemoryManager.getRecentlyUsedApps()

        for (app in runningApps) {
            val predictedAllocation = predictedAllocations[app.packageName] ?: 0L
            if (app.memoryUsage > predictedAllocation * 1.5) {
                // If an app is using 50% more memory than predicted, attempt to optimize it
                optimizeApp(app.packageName)
            }
        }
    }

    private fun optimizeApp(packageName: String) {
        // This is a simplified implementation. In a real app, you'd need to be more careful
        // about which processes you kill and how you manage them.
        // You might also consider using Android's built-in memory trimming mechanisms.
        println("Optimizing app: $packageName")
        // In a real implementation, you might use ActivityManager to trim the app's memory
        // or potentially stop some of its background services
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.background_service_notification))
//            .setSmallIcon(R.drawable.ic_memory)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}