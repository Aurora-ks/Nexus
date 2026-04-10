package com.nexus.app

import android.app.Application

class NexusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.initialize(this)
    }
}
