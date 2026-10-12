package com.example.ctpa.domain.repository



import com.example.ctpa.domain.model.ActivityReport
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

    // Turnos del día (abiertos y cerrados) para el cierre diario
    suspend fun getTodayAttendance(): Flow<List<AttendanceRecord>>

    // Todos los trabajadores registrados
    suspend fun getWorkers(): Flow<List<Worker>>

    // Agregar un nuevo trabajador
    suspend fun addWorker(worker: Worker)

    // Eliminar un trabajador por su docId
    suspend fun deleteWorker(workerId: String)

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

    // Editar una tarea pendiente (título, descripción y fecha máxima)
    suspend fun updatePendingTask(taskId: String, title: String, description: String, deadline: Long)

    // Eliminar una tarea pendiente
    suspend fun deletePendingTask(taskId: String)

    // Todos los pendientes registrados (para el panel del admin)
    suspend fun getAllPendingTasks(): Flow<List<PendingTask>>

    // Reportes de actividad enviados por los trabajadores (reporte diario)
    suspend fun getActivityReports(): Flow<List<ActivityReport>>

    // Alertas de overtime pendientes
    suspend fun getOvertimeAlerts(): Flow<List<AttendanceRecord>>

    // Acciones del admin
    suspend fun approveOvertime(workerId: String)
    suspend fun rejectOvertime(workerId: String)
    suspend fun forceClockOut(workerId: String)
}