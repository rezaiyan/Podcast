package com.hezaro.wall.notification

data class NotificationBody(val id: String = "", val title: String, val message: String, val bigMessage: String = "", val imageUrl: String = "", val url: String = "")