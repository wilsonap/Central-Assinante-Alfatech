package com.example.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationPresentationTest {

    private lateinit var context: Context
    private lateinit var manager: NotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        manager = context.getSystemService(NotificationManager::class.java)
        manager.deleteNotificationChannel(NotificationChannels.CHANNEL_CENTRAL)
        manager.deleteNotificationChannel(NotificationChannels.CHANNEL_INVOICES)
        NotificationChannels.ensureCreated(context)
    }

    @Test
    fun centralChannel_isHighPublicWithBadge() {
        val channel = manager.getNotificationChannel(NotificationChannels.CHANNEL_CENTRAL)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
        assertEquals(Notification.VISIBILITY_PUBLIC, channel.lockscreenVisibility)
        assertTrue(channel.canShowBadge())
    }

    @Test
    fun invoiceChannel_isHighPublicWithBadge() {
        val channel = manager.getNotificationChannel(NotificationChannels.CHANNEL_INVOICES)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
        assertEquals(Notification.VISIBILITY_PUBLIC, channel.lockscreenVisibility)
        assertTrue(channel.canShowBadge())
    }

    @Test
    fun centralBuilder_messageCategoryAndPublicVisibility() {
        val n = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_CENTRAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("t")
            .setContentText("b")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        assertEquals(NotificationCompat.CATEGORY_MESSAGE, n.category)
        assertEquals(Notification.VISIBILITY_PUBLIC, n.visibility)
        assertEquals(NotificationCompat.PRIORITY_HIGH, n.priority)
    }

    @Test
    fun invoiceBuilder_reminderCategoryAndPublicVisibility() {
        val n = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_INVOICES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("t")
            .setContentText("b")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        assertEquals(NotificationCompat.CATEGORY_REMINDER, n.category)
        assertEquals(Notification.VISIBILITY_PUBLIC, n.visibility)
        assertEquals(NotificationCompat.PRIORITY_HIGH, n.priority)
    }

    @Test
    fun existingLoweredChannel_isNotSilentlyUpgraded() {
        manager.deleteNotificationChannel(NotificationChannels.CHANNEL_CENTRAL)
        val lowered = android.app.NotificationChannel(
            NotificationChannels.CHANNEL_CENTRAL,
            "Central",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(lowered)
        NotificationChannels.ensureCreated(context)
        val after = manager.getNotificationChannel(NotificationChannels.CHANNEL_CENTRAL)
        assertEquals(
            "Canal já existente não deve ser elevado silenciosamente",
            NotificationManager.IMPORTANCE_DEFAULT,
            after.importance
        )
    }
}
