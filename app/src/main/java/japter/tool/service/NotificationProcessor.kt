package japter.tool.service

import android.content.Context
import japter.tool.model.NotificationData

interface NotificationProcessor {
    fun getName(): String
    fun process(notificationData: NotificationData, context: Context): String
}