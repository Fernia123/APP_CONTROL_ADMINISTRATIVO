package com.example.ctpa.domain.model

// Estados de una tarea
enum class TaskStatus {
    ACTIVE,       // Pendiente por iniciar
    IN_PROGRESS,  // En proceso
    COMPLETED,    // Terminada y firmada
    DONE          // Archivada/Hecha
}

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val status: TaskStatus = TaskStatus.ACTIVE,
    val estimatedMinutes: Int = 0,
    val elapsedMinutes: Int = 0,
    val signedBy: String? = null,
    val signedTime: String? = null,
    val photoCount: Int = 0,
    val photoUrls: List<String> = emptyList(),
    val workerId: String = "",
    val workerName: String = "",
    val deadline: Long = 0L
)