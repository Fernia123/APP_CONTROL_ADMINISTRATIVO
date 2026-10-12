package com.example.ctpa.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ctpa.domain.model.ActivityReport
import com.example.ctpa.domain.model.AdminStats
import com.example.ctpa.domain.model.AttendanceRecord
import com.example.ctpa.domain.model.PendingTask
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
    val pendingTasks: List<PendingTask> = emptyList(),
    val activityReports: List<ActivityReport> = emptyList(),
    val overtimeAlerts: List<AttendanceRecord> = emptyList(),
    // Turnos del día (abiertos y cerrados) para el cierre diario
    val todayShifts: List<AttendanceRecord> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // Diálogo de nuevo pendiente
    val showAddPendingDialog: Boolean = false,
    // Diálogo de nuevo trabajador
    val showAddWorkerDialog: Boolean = false,
    // Diálogo para eliminar trabajador
    val showDeleteWorkerDialog: Boolean = false,
    // Diálogo de reporte de actividades diarias
    val showActivityReportDialog: Boolean = false,
    // Diálogo con la lista completa de pendientes
    val showAllPendingDialog: Boolean = false,
    // Sección activa del menú: 0 Resumen, 1 Actividades, 2 Trabajadores, 3 Reportes
    val selectedSection: Int = 0,
    // Edición de una actividad pendiente
    val editingPendingTask: PendingTask? = null,
    val showEditPendingDialog: Boolean = false,
    // Cierre del día (actividades realizadas, horas y montos)
    val showDailyClosingDialog: Boolean = false,
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
        loadPendingTasks()
        loadActivityReports()
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

        viewModelScope.launch {
            adminRepository.getTodayAttendance().collect { shifts ->
                _uiState.update { it.copy(todayShifts = shifts) }
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

    private fun loadPendingTasks() {
        viewModelScope.launch {
            adminRepository.getAllPendingTasks().collect { tasks ->
                _uiState.update { it.copy(pendingTasks = tasks) }
            }
        }
    }

    private fun loadActivityReports() {
        viewModelScope.launch {
            adminRepository.getActivityReports().collect { reports ->
                _uiState.update { it.copy(activityReports = reports) }
            }
        }
    }

    // ===== Navegación por secciones (menú) =====
    fun onSectionSelected(index: Int) {
        _uiState.update { it.copy(selectedSection = index) }
    }

    // ===== Diálogo: editar pendiente =====
    fun showEditPendingDialog(task: PendingTask) {
        _uiState.update {
            it.copy(editingPendingTask = task, showEditPendingDialog = true, infoMessage = null)
        }
    }

    fun hideEditPendingDialog() {
        _uiState.update { it.copy(showEditPendingDialog = false, editingPendingTask = null) }
    }

    fun updatePendingTask(title: String, description: String, deadline: Long) {
        val task = _uiState.value.editingPendingTask ?: return
        viewModelScope.launch {
            adminRepository.updatePendingTask(task.id, title, description, deadline)
            _uiState.update {
                it.copy(
                    showEditPendingDialog = false,
                    editingPendingTask = null,
                    infoMessage = "Actividad actualizada correctamente."
                )
            }
        }
    }

    fun deletePendingTask(taskId: String) {
        viewModelScope.launch {
            adminRepository.deletePendingTask(taskId)
            _uiState.update { it.copy(infoMessage = "Actividad eliminada correctamente.") }
        }
    }

    // ===== Cierre del día =====
    fun showDailyClosingDialog() {
        _uiState.update { it.copy(showDailyClosingDialog = true, infoMessage = null) }
    }

    fun hideDailyClosingDialog() {
        _uiState.update { it.copy(showDailyClosingDialog = false) }
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

    // ===== Diálogo: eliminar trabajador =====
    fun showDeleteWorkerDialog() {
        _uiState.update { it.copy(showDeleteWorkerDialog = true, infoMessage = null) }
    }

    fun hideDeleteWorkerDialog() {
        _uiState.update { it.copy(showDeleteWorkerDialog = false) }
    }

    fun deleteWorker(workerId: String) {
        viewModelScope.launch {
            adminRepository.deleteWorker(workerId)
            _uiState.update {
                it.copy(
                    showDeleteWorkerDialog = false,
                    infoMessage = "Trabajador eliminado correctamente."
                )
            }
            loadWorkers()
            loadData()
        }
    }

    // ===== Diálogo: reporte de actividades diarias =====
    fun showActivityReportDialog() {
        _uiState.update { it.copy(showActivityReportDialog = true, infoMessage = null) }
    }

    fun hideActivityReportDialog() {
        _uiState.update { it.copy(showActivityReportDialog = false) }
    }

    // ===== Diálogo: lista completa de pendientes =====
    fun showAllPendingDialog() {
        _uiState.update { it.copy(showAllPendingDialog = true, infoMessage = null) }
    }

    fun hideAllPendingDialog() {
        _uiState.update { it.copy(showAllPendingDialog = false) }
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