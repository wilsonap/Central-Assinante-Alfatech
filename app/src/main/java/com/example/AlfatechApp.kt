package com.example

import android.app.Application
import com.example.notifications.NotificationChannels

/**
 * Garante criação precoce dos NotificationChannels (inclui ID "default" do backend).
 */
class AlfatechApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureCreated(this)
    }
}
