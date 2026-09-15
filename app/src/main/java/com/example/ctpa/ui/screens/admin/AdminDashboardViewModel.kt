package com.example.ctpa.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ctpa.domain.model.AdminStats
import com.example.ctpa.domain.model.AttendanceRecord
import com.example.ctpa.domain.model.Worker
import com.example.ctpa.domain.model.WorkerDetail
import com.example.ctpa.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardUiState(
    val stats: AdminStats = AdminStats(),
    val activeWorkers: List<AttendanceRecord> = emptyList(),
    val workers: List<Worker> = emptyList(),
    val overtimeAlerts: List<AttendanceRecord> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // Diálogo de nuevo pendiente
    val showAddPendingDialog: Boolean = false,
    // Diálogo de nuevo trabajador
    val showAddWorkerDialog: Boolean = false,
    // Detalle de trabajador seleccionado
    val selectedWorkerDetail: WorkerDetail? = null,
    val showWorkerDetail: Boolean = false,
    val infoMessage: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
        loadWorkers()
    }

    private fun loadData() {
        viewModelScope.launch {
            adminRepository.getAdminStats().collect { stats ->
                _uiState.update { it.copy(stats = stats, isLoading = false) }
            }
        }

        viewModelScope.launch {
            adminRepository.getActiveWorkers().collect { workers ->
                _uiState.update { it.copy(activeWorkers = workers) }
            }
        }

        viewModelScope.launch {
            adminRepository.getOvertimeAlerts().collect { alerts ->
                _uiState.update { it.copy(overtimeAlerts = alerts) }
            }
        }
    }

    private fun loadWorkers() {
        viewModelScope.launch {
            adminRepository.getWorkers().collect { workers ->
                _uiState.update { it.copy(workers = workers) }
            }
        }
    }

    // ===== Diálogo: nuevo pendiente =====
    fun showAddPendingDialog() {
        _uiState.update { it.copy(showAddPendingDialog = true, infoMessage = null) }
    }

    fun hideAddPendingDialog() {
        _uiState.update { it.copy(showAddPendingDialog = false) }
    }

    fun addPendingTask(
        workerId: String,
        title: String,
        description: String,
        deadline: Long
    ) {
        viewModelScope.launch {
            adminRepository.addPendingTask(workerId, title, description, deadline)
            _uiState.update {
                it.copy(
                    showAddPendingDialog = false,
                    infoMessage = "Pendiente agregado correctamente."
                )
            }
        }
    }

    // ===== Diálogo: nuevo trabajador =====
    fun showAddWorkerDialog() {
        _uiState.update { it.copy(showAddWorkerDialog = true, infoMessage = null) }
    }

    fun hideAddWorkerDialog() {
        _uiState.update { it.copy(showAddWorkerDialog = false) }
    }

    fun addWorker(worker: Worker) {
        viewModelScope.launch {
            adminRepository.addWorker(worker)
            _uiState.update {
                it.copy(
                    showAddWorkerDialog = false,
                    infoMessage = "Trabajador agregado correctamente."
                )
            }
            loadWorkers()
            loadData()
        }
    }

    // ===== Detalle de trabajador =====
    fun onWorkerSelected(workerId: String) {
        viewModelScope.launch {
            adminRepository.getWorkerDetail(workerId).collect { detail ->
                _uiState.update {
                    it.copy(selectedWorkerDetail = detail, showWorkerDetail = detail != null)
                }
            }
        }
    }

    fun hideWorkerDetail() {
        _uiState.update { it.copy(showWorkerDetail = false, selectedWorkerDetail = null) }
    }

    fun completePendingTask(taskId: String, workerId: String) {
        viewModelScope.launch {
            adminRepository.completePendingTask(workerId, taskId)
            onWorkerSelected(workerId)
        }
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    // ===== Overtime =====
    fun approveOvertime(workerId: String) {
        viewModelScope.launch {
            adminRepository.approveOvertime(workerId)
        }
    }

    fun rejectOvertime(workerId: String) {
        viewModelScope.launch {
            adminRepository.rejectOvertime(workerId)
        }
    }

    fun forceClockOut(workerId: String) {
        viewModelScope.launch {
            adminRepository.forceClockOut(workerId)
        }
    }
}