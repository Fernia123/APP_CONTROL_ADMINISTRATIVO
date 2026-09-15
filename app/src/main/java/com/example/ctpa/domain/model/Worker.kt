package com.example.ctpa.domain.model

data class Worker(
    val id: String = "",
    val docId: String = "",
    val name: String = "",
    val pin: String = "",
    val photoUrl: String = "",
    val hourlyRate: Double = 0.0,
    val maxDailyHours: Int = 8,
    val isActive: Boolean = false,
    val shiftStart: String = "",
    val shiftEnd: String = "",
    val facility: String = ""
)