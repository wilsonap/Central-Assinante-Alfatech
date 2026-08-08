package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["messageId"], unique = true)
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "general",
    /** ID estável do FCM/servidor quando disponível (único no Room). */
    val messageId: String? = null,
    /** Hash de title|body|type para dedupe sem messageId. */
    val contentHash: String = "",
    val targetUrl: String? = null
)
