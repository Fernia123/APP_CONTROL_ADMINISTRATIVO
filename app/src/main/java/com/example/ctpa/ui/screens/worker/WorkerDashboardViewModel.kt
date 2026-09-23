package com.example.ctpa.ui.screens.worker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ctpa.domain.model.Task
import com.example.ctpa.domain.model.TaskStatus
import com.example.ctpa.domain.repository.AuthRepository
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

data class WorkerDashboardUiState(
    val workerName: String = "",
    val facility: String = "",
    val elapsedSeconds: Long = 0L,
    val startTime: String = "--:--",
    val targetTime: String = "8h 00m",
    val isOnBreak: Boolean = false,
    val isClockedOut: Boolean = true,
    val selectedTab: Int = 0, // 0: All, 1: Active, 2: Done
    val selectedSection: Int = 0, // 0: Tasks, 1: Schedule, 2: History, 3: Profile
    val tasks: List<Task> = emptyList(),
    val message: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class WorkerDashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    val workerId: String = savedStateHandle.get<String>("workerId") ?: ""
    private val _uiState = MutableStateFlow(WorkerDashboardUiState())
    val uiState: StateFlow<WorkerDashboardUiState> = _uiState.asStateFlow()

    private var clockInTimestamp: Long = 0L
    private var attendanceDocId: String? = null
    private var timerJob: Job? = null

    init {
        loadWorkerData()
        loadTasks()
        checkActiveAttendance()
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
                            targetTime = "${worker.maxDailyHours}h 00m"
                        )
                    }
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
                    val status = if (statusStr == "COMPLETED") TaskStatus.COMPLETED else TaskStatus.ACTIVE
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
                } else {
                    _uiState.update {
                        it.copy(isClockedOut = true, isOnBreak = false, elapsedSeconds = 0L)
                    }
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

    fun refreshLog() {
        loadTasks()
        checkActiveAttendance()
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
                attendanceDocId?.let { docId ->
                    firestore.collection("attendance").document(docId)
                        .update(
                            mapOf(
                                "clockOutTime" to System.currentTimeMillis(),
                                "status" to "CLOCKED_OUT"
                            )
                        ).await()
                }
                attendanceDocId = null
                clockInTimestamp = 0L
                timerJob?.cancel()

                _uiState.update {
                    it.copy(
                        isClockedOut = true,
                        isOnBreak = false,
                        elapsedSeconds = 0L,
                        message = "Salida registrada con éxito."
                    )
                }
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
                    "location" to _uiState.value.facility
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