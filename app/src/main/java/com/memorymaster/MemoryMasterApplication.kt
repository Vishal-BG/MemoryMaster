package com.memorymaster

import android.app.Application

class MemoryMasterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MemoryManager.init(this)
    }
}