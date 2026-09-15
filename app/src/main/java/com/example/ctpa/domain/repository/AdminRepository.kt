package com.example.ctpa.domain.repository



import com.example.ctpa.domain.model.AdminStats
import com.example.ctpa.domain.model.AttendanceRecord
import com.example.ctpa.domain.model.PendingTask
import com.example.ctpa.domain.model.Worker
import com.example.ctpa.domain.model.WorkerDetail
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    // Estadísticas en tiempo real
    suspend fun getAdminStats(): Flow<AdminStats>

    // Trabajadores activos con su estado
    suspend fun getActiveWorkers(): Flow<List<AttendanceRecord>>

    // Todos los trabajadores registrados
    suspend fun getWorkers(): Flow<List<Worker>>

    // Agregar un nuevo trabajador
    suspend fun addWorker(worker: Worker)

    // Detalle de un trabajador (info + actividades en curso y pendientes)
    suspend fun getWorkerDetail(workerId: String): Flow<WorkerDetail?>

    // Agregar tarea pendiente con fecha máxima de realización
    suspend fun addPendingTask(
        workerId: String,
        title: String,
        description: String,
        deadline: Long
    )

    // Complea una tarea pendiente
    suspend fun completePendingTask(workerId: String, taskId: String)

    // Alertas de overtime pendientes
    suspend fun getOvertimeAlerts(): Flow<List<AttendanceRecord>>

    // Acciones del admin
    suspend fun approveOvertime(workerId: String)
    suspend fun rejectOvertime(workerId: String)
    suspend fun forceClockOut(workerId: String)
}