package com.example.ctpa.ui.screens.worker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ctpa.domain.model.Task
import com.example.ctpa.domain.model.TaskStatus
import com.example.ctpa.domain.model.WorkerStatus
import com.example.ctpa.domain.repository.AuthRepository
import com.example.ctpa.util.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DailyShift(
    val dateKey: String = "",
    val label: String = "",
    val clockInTime: Long = 0L,
    val elapsedMinutes: Int = 0,
    val rate: Double = 0.0,
    val pay: Double = 0.0,
    val maxMinutes: Int = 480,
    val overtimeMinutes: Int = 0
) {
    val isOvertime: Boolean get() = overtimeMinutes > 0
}

data class WorkerDashboardUiState(
    val workerName: String = "",
    val facility: String = "",
    val hourlyRate: Double = 0.0,
    val maxDailyHours: Int = 8,
    val elapsedSeconds: Long = 0L,
    val startTime: String = "--:--",
    val targetTime: String = "8h 00m",
    val isOnBreak: Boolean = false,
    val isClockedOut: Boolean = true,
    val selectedTab: Int = 0, // 0: All, 1: Active, 2: Done
    val selectedSection: Int = 0, // 0: Tasks, 1: Schedule, 2: History, 3: Profile
    val tasks: List<Task> = emptyList(),
    // Totales conservados desde los registros de asistencia (últimos 7 días)
    val weekHours: Double = 0.0,
    val weekPay: Double = 0.0,
    val weekShifts: Int = 0,
    // Detalle por día: horas realizadas y monto a pagar
    val dailyShifts: List<DailyShift> = emptyList(),
    // Estado de revisión de horas extra: "approved" | "rejected" | null (en revisión)
    val overtimeStatus: String? = null,
    val message: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class WorkerDashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    val workerId: String = savedStateHandle.get<String>("workerId") ?: ""
    private val _uiState = MutableStateFlow(WorkerDashboardUiState())
    val uiState: StateFlow<WorkerDashboardUiState> = _uiState.asStateFlow()

    private var clockInTimestamp: Long = 0L
    private var attendanceDocId: String? = null
    private var timerJob: Job? = null
    private var reminderJob: Job? = null

    companion object {
        // Intervalo entre recordatorios de actividad mientras el trabajador está en turno
        private const val REMINDER_INTERVAL_MS = 60_000L
    }

    init {
        loadWorkerData()
        loadTasks()
        checkActiveAttendance()
        loadHistory()
    }

    private fun loadWorkerData() {
        if (workerId.isBlank()) return
        viewModelScope.launch {
            authRepository.getWorker(workerId).collect { worker ->
                if (worker != null) {
                    _uiState.update {
                        it.copy(
                            workerName = worker.name,
                            facility = worker.facility,
                            hourlyRate = worker.hourlyRate,
                            maxDailyHours = worker.maxDailyHours,
                            targetTime = "${worker.maxDailyHours}h 00m"
                        )
                    }
                }
            }
        }
    }

    /** Carga el historial: horas y pago por día (últimos 7) y totales de la semana. */
    private fun loadHistory() {
        if (workerId.isBlank()) return
        viewModelScope.launch {
            // Estado de revisión de horas extra (approved / rejected / en revisión)
            try {
                val otDoc = firestore.collection("overtime_requests").document(workerId).get().await()
                _uiState.update { it.copy(overtimeStatus = otDoc.getString("status")) }
            } catch (_: Exception) { }

            authRepository.getAttendanceHistory(workerId).collect { records ->
                val now = System.currentTimeMillis()
                val rate = _uiState.value.hourlyRate
                val maxMinutes = _uiState.value.maxDailyHours * 60
                val closedShifts = records.filter { it.status == WorkerStatus.CLOCKED_OUT }

                val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dayLabelFormat = SimpleDateFormat("EEE dd MMM", Locale.getDefault())

                // Agrupa los turnos cerrados por día y calcula horas + monto de cada día.
                // El cobro es por horas: minutos trabajados / 60 * tarifa por hora.
                val daily = closedShifts
                    .groupBy { dayKeyFormat.format(Date(it.clockInTime)) }
                    .map { (key, dayRecords) ->
                        val mins = dayRecords.sumOf { it.elapsedMinutes }
                        // Tarifa del día (si el registro guarda monto, se respeta ese cobro)
                        val storedPay = dayRecords.sumOf { it.currentPay }
                        val pay = if (rate > 0.0) (mins / 60.0) * rate else storedPay
                        val firstClockIn = dayRecords.minOf { it.clockInTime }
                        DailyShift(
                            dateKey = key,
                            label = dayLabelFormat.format(Date(firstClockIn)),
                            clockInTime = firstClockIn,
                            elapsedMinutes = mins,
                            rate = rate,
                            pay = pay,
                            maxMinutes = maxMinutes,
                            overtimeMinutes = maxOf(0, mins - maxMinutes)
                        )
                    }
                    .sortedByDescending { it.clockInTime }

                val weekStart = now - 7L * 24 * 60 * 60 * 1000
                val weekShifts = closedShifts.filter { it.clockInTime >= weekStart }

                _uiState.update {
                    it.copy(
                        weekHours = weekShifts.sumOf { r -> r.elapsedMinutes } / 60.0,
                        weekPay = weekShifts.sumOf { r ->
                            if (rate > 0.0) (r.elapsedMinutes / 60.0) * rate else r.currentPay
                        },
                        weekShifts = weekShifts.size,
                        dailyShifts = daily.take(7)
                    )
                }
            }
        }
    }

    private fun loadTasks() {
        if (workerId.isBlank()) return
        viewModelScope.launch {
            try {
                // Tareas principales
                val tasksSnapshot = firestore.collection("tasks")
                    .whereEqualTo("workerId", workerId)
                    .get()
                    .await()

                val tasksList = tasksSnapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Task::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }

                // Tareas pendientes asignadas
                val pendingSnapshot = firestore.collection("pending_tasks")
                    .whereEqualTo("workerId", workerId)
                    .get()
                    .await()

                val pendingTasksList = pendingSnapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: ""
                    val desc = doc.getString("description") ?: ""
                    val statusStr = doc.getString("status") ?: "PENDING"
                    val status = when (statusStr) {
                        "COMPLETED" -> TaskStatus.COMPLETED
                        "IN_PROGRESS" -> TaskStatus.IN_PROGRESS
                        else -> TaskStatus.ACTIVE
                    }
                    Task(
                        id = doc.id,
                        title = title,
                        description = desc,
                        status = status,
                        workerId = workerId
                    )
                }

                val combinedTasks = (tasksList + pendingTasksList).distinctBy { it.id }
                _uiState.update { it.copy(tasks = combinedTasks, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun checkActiveAttendance() {
        if (workerId.isBlank()) return
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("attendance")
                    .whereEqualTo("workerId", workerId)
                    .whereEqualTo("clockOutTime", null)
                    .limit(1)
                    .get()
                    .await()

                if (snapshot.documents.isNotEmpty()) {
                    val doc = snapshot.documents.first()
                    attendanceDocId = doc.id
                    val clockIn = doc.getLong("clockInTime") ?: System.currentTimeMillis()
                    clockInTimestamp = clockIn
                    val statusStr = doc.getString("status") ?: "ACTIVE"
                    val isOnBreak = statusStr == "ON_BREAK"

                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val startStr = sdf.format(Date(clockIn))
                    val elapsed = (System.currentTimeMillis() - clockIn) / 1000

                    _uiState.update {
                        it.copy(
                            isClockedOut = false,
                            isOnBreak = isOnBreak,
                            startTime = startStr,
                            elapsedSeconds = if (elapsed > 0) elapsed else 0L
                        )
                    }
                    startTimer()
                    startActivityReminders()
                } else {
                    _uiState.update {
                        it.copy(isClockedOut = true, isOnBreak = false, elapsedSeconds = 0L)
                    }
                    stopActivityReminders()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isClockedOut = true) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val state = _uiState.value
                if (state.isClockedOut) break
                if (!state.isOnBreak && clockInTimestamp > 0) {
                    val currentElapsed = (System.currentTimeMillis() - clockInTimestamp) / 1000
                    _uiState.update { it.copy(elapsedSeconds = currentElapsed) }
                }
            }
        }
    }

    /** Mientras el trabajador está en turno, recuerda sus actividades pendientes. */
    private fun startActivityReminders() {
        if (reminderJob?.isActive == true) return
        reminderJob = viewModelScope.launch {
            while (isActive) {
                val state = _uiState.value
                if (state.isClockedOut) break
                val pending = state.tasks.filter {
                    it.status == TaskStatus.ACTIVE || it.status == TaskStatus.IN_PROGRESS
                }
                if (pending.isNotEmpty()) {
                    notificationHelper.notifyActivityReminder(
                        workerName = state.workerName,
                        pendingTitles = pending.map { it.title.ifBlank { "Actividad pendiente" } }
                    )
                }
                delay(REMINDER_INTERVAL_MS)
            }
        }
    }

    private fun stopActivityReminders() {
        reminderJob?.cancel()
        reminderJob = null
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun toggleBreak() {
        if (_uiState.value.isClockedOut) return
        viewModelScope.launch {
            val newBreakState = !_uiState.value.isOnBreak
            _uiState.update { it.copy(isOnBreak = newBreakState) }

            attendanceDocId?.let { docId ->
                try {
                    firestore.collection("attendance").document(docId)
                        .update("status", if (newBreakState) "ON_BREAK" else "ACTIVE")
                        .await()
                } catch (e: Exception) { }
            }
        }
    }

    fun onSectionSelected(index: Int) {
        _uiState.update { it.copy(selectedSection = index) }
    }

    /**
     * Marca una actividad como "en proceso" o "completada" y deja un mensaje
     * opcional que el admin verá en el reporte de actividades diarias.
     */
    fun updateTaskStatus(task: Task, newStatus: TaskStatus, message: String) {
        viewModelScope.launch {
            try {
                val statusName = when (newStatus) {
                    TaskStatus.IN_PROGRESS -> "IN_PROGRESS"
                    TaskStatus.COMPLETED, TaskStatus.DONE -> "COMPLETED"
                    TaskStatus.ACTIVE -> "PENDING"
                }

                // Actualizar la tarea en la colección donde exista (best-effort)
                for (collection in listOf("pending_tasks", "tasks")) {
                    try {
                        firestore.collection(collection).document(task.id)
                            .update("status", statusName)
                            .await()
                    } catch (_: Exception) { /* Puede no existir en esa colección */ }
                }

                // Registrar el reporte de actividad para el admin
                firestore.collection("activity_reports").add(
                    mapOf(
                        "workerId" to workerId,
                        "workerName" to _uiState.value.workerName,
                        "taskId" to task.id,
                        "taskTitle" to task.title,
                        "status" to statusName,
                        "message" to message.trim(),
                        "timestamp" to System.currentTimeMillis()
                    )
                ).await()

                // Si terminó la actividad, avisar al administrador (trabajador + tarea)
                if (newStatus == TaskStatus.COMPLETED || newStatus == TaskStatus.DONE) {
                    val workerName = _uiState.value.workerName
                    try {
                        firestore.collection("admin_notifications").add(
                            mapOf(
                                "type" to "TASK_COMPLETED",
                                "workerId" to workerId,
                                "workerName" to workerName,
                                "taskId" to task.id,
                                "taskTitle" to task.title,
                                "message" to message.trim(),
                                "timestamp" to System.currentTimeMillis(),
                                "read" to false
                            )
                        ).await()
                    } catch (_: Exception) { }

                    notificationHelper.notifyAdminTaskCompleted(
                        workerName = workerName,
                        taskTitle = task.title
                    )
                }

                loadTasks()
                _uiState.update {
                    it.copy(
                        message = if (newStatus == TaskStatus.COMPLETED)
                            "Actividad marcada como completada."
                        else "Actividad marcada en proceso."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Error al actualizar la actividad: ${e.message}") }
            }
        }
    }

    fun refreshLog() {
        loadTasks()
        checkActiveAttendance()
        loadHistory()
        _uiState.update {
            it.copy(message = "Logs sincronizados con la base de datos.")
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clockOut() {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val elapsedMins = if (clockInTimestamp > 0) {
                    maxOf(0, ((now - clockInTimestamp) / 60000).toInt())
                } else 0
                val rate = _uiState.value.hourlyRate

                // Pago por horas trabajadas y horas acumuladas, conservados en el registro
                val totalPay = (elapsedMins / 60.0) * rate
                val hoursText = "${elapsedMins / 60}h ${elapsedMins % 60}m"
                val payText = String.format("%.2f", totalPay)

                attendanceDocId?.let { docId ->
                    firestore.collection("attendance").document(docId)
                        .update(
                            mapOf(
                                "clockOutTime" to now,
                                "status" to "CLOCKED_OUT",
                                "hourlyRate" to rate,
                                "elapsedMinutes" to elapsedMins,
                                "totalPay" to totalPay
                            )
                        ).await()
                }
                attendanceDocId = null
                clockInTimestamp = 0L
                timerJob?.cancel()
                stopActivityReminders()

                _uiState.update {
                    it.copy(
                        isClockedOut = true,
                        isOnBreak = false,
                        elapsedSeconds = 0L,
                        message = "Salida registrada: $hoursText • S/. $payText"
                    )
                }
                loadHistory()
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Error al registrar salida: ${e.message}") }
            }
        }
    }

    fun restartShift() {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                clockInTimestamp = now
                val data = mapOf(
                    "workerId" to workerId,
                    "workerName" to _uiState.value.workerName,
                    "clockInTime" to now,
                    "clockOutTime" to null,
                    "status" to "ACTIVE",
                    "location" to _uiState.value.facility,
                    // Se guarda la tarifa para conservar el cálculo del pago
                    "hourlyRate" to _uiState.value.hourlyRate,
                    "maxDailyHours" to _uiState.value.maxDailyHours,
                    "elapsedMinutes" to 0,
                    "totalPay" to 0.0
                )
                val docRef = firestore.collection("attendance").add(data).await()
                attendanceDocId = docRef.id

                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val startStr = sdf.format(Date(now))

                _uiState.update {
                    it.copy(
                        isClockedOut = false,
                        isOnBreak = false,
                        startTime = startStr,
                        elapsedSeconds = 0L,
                        message = "Entrada registrada con éxito."
                    )
                }
                startTimer()
                startActivityReminders()
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Error al iniciar turno: ${e.message}") }
            }
        }
    }

    fun formatTime(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}