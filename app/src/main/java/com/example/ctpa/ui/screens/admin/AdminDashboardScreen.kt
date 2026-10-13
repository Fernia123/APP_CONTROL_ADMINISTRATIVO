package com.example.ctpa.ui.screens.admin


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ctpa.domain.model.ActivityReport
import com.example.ctpa.domain.model.AdminStats
import com.example.ctpa.domain.model.AdminNotification
import com.example.ctpa.domain.model.AttendanceRecord
import com.example.ctpa.domain.model.PendingTask
import com.example.ctpa.domain.model.PendingTaskStatus
import com.example.ctpa.domain.model.Worker
import com.example.ctpa.domain.model.WorkerDetail
import com.example.ctpa.ui.components.*
import com.example.ctpa.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminDashboardContent(
        uiState = uiState,
        onBack = onBack,
        isDark = themeViewModel.isDarkMode(),
        onToggleDark = themeViewModel::setDarkMode,
        onWorkerSelected = viewModel::onWorkerSelected,
        onHideWorkerDetail = viewModel::hideWorkerDetail,
        onShowAddPending = viewModel::showAddPendingDialog,
        onHideAddPending = viewModel::hideAddPendingDialog,
        onAddPendingTask = viewModel::addPendingTask,
        onShowAddWorker = viewModel::showAddWorkerDialog,
        onHideAddWorker = viewModel::hideAddWorkerDialog,
        onAddWorker = viewModel::addWorker,
        onCompletePendingTask = viewModel::completePendingTask,
        onClearInfoMessage = viewModel::clearInfoMessage,
        onApproveOvertime = viewModel::approveOvertime,
        onRejectOvertime = viewModel::rejectOvertime,
        onForceClockOut = viewModel::forceClockOut,
        onShowDeleteWorker = viewModel::showDeleteWorkerDialog,
        onHideDeleteWorker = viewModel::hideDeleteWorkerDialog,
        onDeleteWorker = viewModel::deleteWorker,
        onShowActivityReport = viewModel::showActivityReportDialog,
        onHideActivityReport = viewModel::hideActivityReportDialog,
        onShowAllPending = viewModel::showAllPendingDialog,
        onHideAllPending = viewModel::hideAllPendingDialog,
        onSectionSelected = viewModel::onSectionSelected,
        onShowEditPending = viewModel::showEditPendingDialog,
        onHideEditPending = viewModel::hideEditPendingDialog,
        onUpdatePendingTask = viewModel::updatePendingTask,
        onDeletePendingTask = viewModel::deletePendingTask,
        onShowDailyClosing = viewModel::showDailyClosingDialog,
        onHideDailyClosing = viewModel::hideDailyClosingDialog
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardContent(
    uiState: AdminDashboardUiState,
    onBack: () -> Unit = {},
    isDark: Boolean = false,
    onToggleDark: ((Boolean) -> Unit)? = null,
    onWorkerSelected: (String) -> Unit = {},
    onHideWorkerDetail: () -> Unit = {},
    onShowAddPending: () -> Unit = {},
    onHideAddPending: () -> Unit = {},
    onAddPendingTask: (String, String, String, Long) -> Unit = { _, _, _, _ -> },
    onShowAddWorker: () -> Unit = {},
    onHideAddWorker: () -> Unit = {},
    onAddWorker: (Worker) -> Unit = {},
    onCompletePendingTask: (String, String) -> Unit = { _, _ -> },
    onClearInfoMessage: () -> Unit = {},
    onApproveOvertime: (String) -> Unit = {},
    onRejectOvertime: (String) -> Unit = {},
    onForceClockOut: (String) -> Unit = {},
    onShowDeleteWorker: () -> Unit = {},
    onHideDeleteWorker: () -> Unit = {},
    onDeleteWorker: (String) -> Unit = {},
    onShowActivityReport: () -> Unit = {},
    onHideActivityReport: () -> Unit = {},
    onShowAllPending: () -> Unit = {},
    onHideAllPending: () -> Unit = {},
    onSectionSelected: (Int) -> Unit = {},
    onShowEditPending: (PendingTask) -> Unit = {},
    onHideEditPending: () -> Unit = {},
    onUpdatePendingTask: (String, String, Long) -> Unit = { _, _, _ -> },
    onDeletePendingTask: (String) -> Unit = {},
    onShowDailyClosing: () -> Unit = {},
    onHideDailyClosing: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Regresar",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AREA DE OPERACIONES",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray500
                        )
                    }
                },
                actions = {
                    Text(
                        text = "CAMBIAR COLOR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    // Interruptor de modo oscuro
                    IconButton(onClick = { onToggleDark?.invoke(!isDark) }) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = if (isDark) "Modo claro" else "Modo oscuro",
                            tint = Gray700
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                    label = { Text("Resumen") },
                    selected = uiState.selectedSection == 0,
                    onClick = { onSectionSelected(0) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Checklist, contentDescription = null) },
                    label = { Text("Actividades") },
                    selected = uiState.selectedSection == 1,
                    onClick = { onSectionSelected(1) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Groups, contentDescription = null) },
                    label = { Text("Trabajadores") },
                    selected = uiState.selectedSection == 2,
                    onClick = { onSectionSelected(2) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Assessment, contentDescription = null) },
                    label = { Text("Reportes") },
                    selected = uiState.selectedSection == 3,
                    onClick = { onSectionSelected(3) }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Gray50)
        ) {
            // Selector de fecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Hoy, ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "EN / ES",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gray500
                )
            }

            uiState.infoMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald50)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Emerald500, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray700
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LaunchedEffect(message) { onClearInfoMessage() }
            }

            if (uiState.selectedSection == 0) {
            // ===== KPIs GRID =====
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        label = "Total de Trabajadores Activos ",
                        value = "${uiState.stats.totalActiveWorkers}",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Groups,
                        accent = Emerald500,
                        detail = "${uiState.activeWorkers.size} en turno ahora"
                    )
                    KpiCard(
                        label = "Total Monto del Día",
                        value = "s/.${String.format("%.2f", uiState.stats.totalPayrollToday)}",
                        modifier = Modifier.weight(1f),
                        valueColor = Emerald600,
                        icon = Icons.Filled.Payments,
                        accent = Emerald600,
                        detail = "Acumulado del día"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        label = "Total de Horas Trabajadas",
                        value = "${String.format("%.2f", uiState.stats.totalHoursLogged)} hrs",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.HourglassBottom,
                        accent = Color(0xFF3B82F6),
                        detail = "Sincronizado con Firebase"
                    )
                    KpiCard(
                        label = "Pago por Horas",
                        value = "s/.${String.format("%.2f", uiState.stats.avgRate)}",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.CurrencyExchange,
                        accent = Color(0xFF3B82F6),
                        detail = "Por hora / trabajador"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        label = "Horas Extras",
                        value = uiState.stats.overtime,
                        modifier = Modifier.weight(1f),
                        valueColor = Color(0xFFF59E0B),
                        icon = Icons.Filled.MoreTime,
                        accent = Color(0xFFF59E0B),
                        detail = if (uiState.overtimeAlerts.isNotEmpty()) "${uiState.overtimeAlerts.size} alerta(s) activa(s)" else "Sin excesos hoy"
                    )
                    KpiCard(
                        label = "Revision Pendiente",
                        value = "${uiState.stats.pendingApprovals}",
                        modifier = Modifier.weight(1f),
                        valueColor = Red500,
                        icon = Icons.Filled.PendingActions,
                        accent = Red500,
                        detail = if (uiState.stats.pendingApprovals > 0) "Requiere revisión" else "Todo al día"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== CIERRE DEL DÍA =====
            Button(
                onClick = onShowDailyClosing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
            ) {
                Icon(
                    Icons.Filled.EventAvailable,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cierre del día", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== ALERTAS DE OVERTIME =====
            if (uiState.overtimeAlerts.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Alertas críticas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    uiState.overtimeAlerts.forEach { alert ->
                        OvertimeAlertCard(
                            alert = alert,
                            onApprove = { onApproveOvertime(alert.workerId) },
                            onReject = { onRejectOvertime(alert.workerId) },
                            onForceClockOut = { onForceClockOut(alert.workerId) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== TRABAJADORES ACTIVOS =====
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trabajadores activos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${uiState.activeWorkers.size} en línea",
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald600,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.activeWorkers.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "No hay trabajadores activos",
                            color = Gray500,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }

                uiState.activeWorkers.forEach { worker ->
                    ActiveWorkerRow(
                        worker = worker,
                        onClick = { onWorkerSelected(worker.workerId) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== GPS RADAR =====
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                GpsRadar()
            }
            } // fin sección Resumen

            when (uiState.selectedSection) {
                1 -> ActivitiesSection(
                    uiState = uiState,
                    onShowAddPending = onShowAddPending,
                    onShowAllPending = onShowAllPending,
                    onCompletePendingTask = onCompletePendingTask,
                    onShowEditPending = onShowEditPending,
                    onDeletePendingTask = onDeletePendingTask,
                    onWorkerSelected = onWorkerSelected
                )
                2 -> WorkersSection(
                    uiState = uiState,
                    onShowAddWorker = onShowAddWorker,
                    onShowDeleteWorker = onShowDeleteWorker,
                    onDeleteWorker = onDeleteWorker,
                    onWorkerSelected = onWorkerSelected
                )
                3 -> ReportsSection(uiState = uiState, onShowDailyClosing = onShowDailyClosing)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        LoadingOverlay(
            visible = uiState.isLoading,
            message = "Sincronizando datos..."
        )
        }
    }

    // ===== DIÁLOGO: NUEVO PENDIENTE =====
    if (uiState.showAddPendingDialog) {
        AddPendingTaskDialog(
            workers = uiState.workers,
            defaultWorkerId = uiState.selectedWorkerDetail?.worker?.docId
                ?: uiState.activeWorkers.firstOrNull()?.workerId
                ?: "",
            onDismiss = onHideAddPending,
            onSave = onAddPendingTask
        )
    }

    // ===== DIÁLOGO: EDITAR PENDIENTE =====
    if (uiState.showEditPendingDialog) {
        uiState.editingPendingTask?.let { task ->
            EditPendingTaskDialog(
                task = task,
                onDismiss = onHideEditPending,
                onSave = onUpdatePendingTask
            )
        }
    }

    // ===== DIÁLOGO: CIERRE DEL DÍA =====
    if (uiState.showDailyClosingDialog) {
        DailyClosingDialog(
            shifts = uiState.todayShifts,
            reports = uiState.activityReports,
            onDismiss = onHideDailyClosing
        )
    }

    // ===== DIÁLOGO: NUEVO TRABAJADOR =====
    if (uiState.showAddWorkerDialog) {
        AddWorkerDialog(
            onDismiss = onHideAddWorker,
            onSave = onAddWorker
        )
    }

    // ===== DIÁLOGO: DETALLE DE TRABAJADOR =====
    if (uiState.showWorkerDetail) {
        uiState.selectedWorkerDetail?.let { detail ->
            WorkerDetailDialog(
                detail = detail,
                onDismiss = onHideWorkerDetail,
                onCompleteTask = { taskId -> onCompletePendingTask(taskId, detail.worker.docId) }
            )
        }
    }

    // ===== DIÁLOGO: ELIMINAR TRABAJADOR =====
    if (uiState.showDeleteWorkerDialog) {
        DeleteWorkerDialog(
            workers = uiState.workers,
            onDismiss = onHideDeleteWorker,
            onDelete = onDeleteWorker
        )
    }

    // ===== DIÁLOGO: REPORTE DE ACTIVIDADES DIARIAS =====
    if (uiState.showActivityReportDialog) {
        ActivityReportDialog(
            reports = uiState.activityReports,
            onDismiss = onHideActivityReport
        )
    }

    // ===== DIÁLOGO: TODOS LOS PENDIENTES =====
    if (uiState.showAllPendingDialog) {
        AllPendingTasksDialog(
            pendingTasks = uiState.pendingTasks,
            workers = uiState.workers,
            onDismiss = onHideAllPending,
            onComplete = { taskId, workerId -> onCompletePendingTask(taskId, workerId) }
        )
    }
}

// ===== DIÁLOGO ELIMINAR TRABAJADOR =====
@Composable
fun DeleteWorkerDialog(
    workers: List<Worker>,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit
) {
    var selectedWorkerId by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }

    val selectedWorker = workers.firstOrNull { it.docId == selectedWorkerId }

    AppDialog(
        onDismiss = onDismiss,
        icon = Icons.Filled.PersonRemove,
        accent = Red500,
        title = "Eliminar trabajador",
        subtitle = "Selecciona a quién deseas eliminar",
        confirmLabel = if (confirmDelete) "Confirmar eliminación" else null,
        confirmIcon = Icons.Filled.DeleteForever,
        onConfirm = {
            if (confirmDelete && selectedWorkerId.isNotEmpty()) {
                onDelete(selectedWorkerId)
            }
        },
        contentMaxHeight = 460.dp
    ) {
        if (workers.isEmpty()) {
            EmptyHint("No hay trabajadores registrados")
        } else {
            workers.forEach { worker ->
                val isSelected = worker.docId == selectedWorkerId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedWorkerId = worker.docId
                            confirmDelete = false
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Red50 else MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = if (isSelected) BorderStroke(1.dp, Red500) else null
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(
                            icon = Icons.Filled.Person,
                            tint = if (isSelected) Red500 else Gray500,
                            size = 34.dp,
                            iconSize = 16.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                worker.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray900
                            )
                            Text(
                                "#${worker.id} • ${worker.facility.ifEmpty { "Sin facility" }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500
                            )
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Filled.RadioButtonChecked,
                                contentDescription = null,
                                tint = Red500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (selectedWorker != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Red50)
                        .clickable { confirmDelete = !confirmDelete }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = confirmDelete,
                        onCheckedChange = { confirmDelete = it },
                        colors = CheckboxDefaults.colors(checkedColor = Red500)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Confirmo eliminar a ${selectedWorker.name}. Esta acción no se puede deshacer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray700
                    )
                }
            }
        }
    }
}

// ===== DIÁLOGO REPORTE DE ACTIVIDADES DIARIAS =====
@Composable
fun ActivityReportDialog(
    reports: List<ActivityReport>,
    onDismiss: () -> Unit
) {
    val now = System.currentTimeMillis()
    val startOfDay = remember(now) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    val todayReports = reports.filter { it.timestamp >= startOfDay }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    AppDialog(
        onDismiss = onDismiss,
        icon = Icons.Filled.Assessment,
        accent = Color(0xFF3B82F6),
        title = "Reporte de actividades diarias",
        subtitle = "${todayReports.size} interacción(es) de hoy",
        contentMaxHeight = 520.dp
    ) {
        if (todayReports.isEmpty()) {
            EmptyHint("Aún no hay reportes de actividad de hoy")
        } else {
            todayReports.forEach { report ->
                val isCompleted = report.status == "COMPLETED"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) Emerald50 else Amber50
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircleIconBadge(
                                icon = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Autorenew,
                                tint = if (isCompleted) Emerald500 else Color(0xFFD97706),
                                size = 30.dp,
                                iconSize = 15.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    report.taskTitle.ifBlank { "Actividad" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gray900,
                                    maxLines = 2
                                )
                                Text(
                                    "${report.workerName.ifBlank { report.workerId }} • ${
                                        if (isCompleted) "Completada" else "En proceso"
                                    }",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray500
                                )
                            }
                            Text(
                                timeFormat.format(Date(report.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500
                            )
                        }
                        if (report.message.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Filled.Chat,
                                    contentDescription = null,
                                    tint = Gray400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    report.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray700
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===== DIÁLOGO TODOS LOS PENDIENTES =====
@Composable
fun AllPendingTasksDialog(
    pendingTasks: List<PendingTask>,
    workers: List<Worker>,
    onDismiss: () -> Unit,
    onComplete: (taskId: String, workerId: String) -> Unit
) {
    val now = System.currentTimeMillis()
    val workersMap = remember(workers) { workers.associateBy { it.docId.ifEmpty { it.id } } }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val pendingCount = pendingTasks.count { it.status == PendingTaskStatus.PENDING }
    val inProgressCount = pendingTasks.count { it.status == PendingTaskStatus.IN_PROGRESS }

    AppDialog(
        onDismiss = onDismiss,
        icon = Icons.Filled.PendingActions,
        accent = Color(0xFFF59E0B),
        title = "Todas las actividades pendientes",
        subtitle = "$pendingCount pendientes • $inProgressCount en proceso",
        contentMaxHeight = 520.dp
    ) {
        if (pendingTasks.isEmpty()) {
            EmptyHint("No hay pendientes registrados")
        } else {
            pendingTasks.forEach { task ->
                val workerName = workersMap[task.workerId]?.name
                    ?: task.workerId.ifBlank { "Sin asignar" }
                val isOverdue = task.deadline in 1..now
                val isInProgress = task.status == PendingTaskStatus.IN_PROGRESS

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isOverdue -> Red50
                            isInProgress -> Amber50
                            else -> Emerald50
                        }
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray900,
                                maxLines = 2
                            )
                            Text(
                                "Asignado a $workerName",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isInProgress) Color(0xFFF59E0B).copy(alpha = 0.15f)
                                            else Emerald500.copy(alpha = 0.15f)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isInProgress) "En proceso" else "Pendiente",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isInProgress) Color(0xFFD97706) else Emerald700,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (task.deadline > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = (if (isOverdue) "Vencido: " else "Máx. ") +
                                            dateFormat.format(Date(task.deadline)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isOverdue) Red500 else Gray500,
                                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        TextButton(onClick = { onComplete(task.id, task.workerId) }) {
                            Text(
                                text = "Completar",
                                color = Emerald600,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ===== DIÁLOGO AGREGAR PENDIENTE =====
/** Badge que indica si un trabajador está activo o no. */
@Composable
private fun WorkerStatusBadge(isActive: Boolean) {
    val color = if (isActive) Emerald600 else Gray500
    val background = if (isActive) Emerald50 else Gray100
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isActive) "Activo" else "Inactivo",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPendingTaskDialog(
    workers: List<Worker>,
    defaultWorkerId: String,
    onDismiss: () -> Unit,
    onSave: (workerId: String, title: String, description: String, deadline: Long) -> Unit
) {
    // Se muestran todos los trabajadores registrados (activos e inactivos)
    val availableWorkers = workers

    var selectedWorkerId by remember {
        mutableStateOf(
            defaultWorkerId.ifEmpty {
                availableWorkers.firstOrNull { it.isActive }?.docId
                    ?: availableWorkers.firstOrNull()?.docId
                    ?: ""
            }
        )
    }
    val selectedWorker = availableWorkers.firstOrNull { it.docId == selectedWorkerId }
    val selectedWorkerStatus: (@Composable () -> Unit)? = selectedWorker?.let { worker ->
        {
            Text(
                text = if (worker.isActive) "Trabajador activo" else "Trabajador inactivo",
                color = if (worker.isActive) Emerald600 else Gray500
            )
        }
    }
    var workerMenuExpanded by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var deadline by remember { mutableStateOf<Long?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val deadlineText = deadline?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) }
        ?: "Seleccionar fecha"

    AppDialog(
        onDismiss = onDismiss,
        icon = Icons.Filled.AddTask,
        accent = Emerald500,
        title = "Nueva actividad pendiente",
        subtitle = "Asigna una tarea con fecha límite",
        confirmLabel = "Guardar",
        confirmIcon = Icons.Filled.Save,
        onConfirm = {
            when {
                selectedWorkerId.isEmpty() -> errorText = "Selecciona un trabajador."
                title.isBlank() -> errorText = "Escribe el título de la actividad."
                deadline == null -> errorText = "Selecciona la fecha máxima de realización."
                else -> onSave(selectedWorkerId, title.trim(), description.trim(), deadline!!)
            }
        },
        contentMaxHeight = 460.dp
    ) {
        // Selector de trabajador
        ExposedDropdownMenuBox(
            expanded = workerMenuExpanded,
            onExpandedChange = { workerMenuExpanded = !workerMenuExpanded }
        ) {
            OutlinedTextField(
                value = selectedWorker?.name ?: "Selecciona un trabajador",
                onValueChange = {},
                readOnly = true,
                label = { Text("Trabajador") },
                leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = workerMenuExpanded)
                },
                supportingText = selectedWorkerStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = workerMenuExpanded,
                onDismissRequest = { workerMenuExpanded = false }
            ) {
                if (availableWorkers.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay trabajadores registrados", color = Gray500) },
                        onClick = { workerMenuExpanded = false }
                    )
                }
                availableWorkers.forEach { worker ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(worker.name, fontWeight = FontWeight.Medium)
                                Text(
                                    "#${worker.id} • ${worker.facility.ifEmpty { "Sin facility" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray500
                                )
                            }
                        },
                        trailingIcon = { WorkerStatusBadge(isActive = worker.isActive) },
                        onClick = {
                            selectedWorkerId = worker.docId
                            workerMenuExpanded = false
                        }
                    )
                }
            }
        }

        // Título
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título de la actividad") },
            leadingIcon = { Icon(Icons.Filled.EditNote, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
        )

        // Descripción
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción (opcional)") },
            leadingIcon = { Icon(Icons.Filled.Notes, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        // Fecha máxima de realización (fila con badge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Emerald50)
                .border(1.dp, Emerald500.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .clickable { showDatePicker = true }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(
                icon = Icons.Filled.Event,
                tint = Emerald600,
                size = 34.dp,
                iconSize = 16.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Día máximo de realización",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500
                )
                Text(
                    text = deadlineText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Emerald700
                )
            }
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = Emerald600,
                modifier = Modifier.size(20.dp)
            )
        }

        errorText?.let { DialogErrorHint(it) }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Fecha límite: fin del día seleccionado
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = millis
                            cal.set(Calendar.HOUR_OF_DAY, 23)
                            cal.set(Calendar.MINUTE, 59)
                            cal.set(Calendar.SECOND, 59)
                            deadline = cal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar", color = Emerald600, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = Gray500)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ===== DIÁLOGO AGREGAR TRABAJADOR =====
@Composable
fun AddWorkerDialog(
    onDismiss: () -> Unit,
    onSave: (Worker) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var id by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var hourlyRateText by remember { mutableStateOf("") }
    var maxDailyHoursText by remember { mutableStateOf("") }
    var facility by remember { mutableStateOf("") }
    var shiftStart by remember { mutableStateOf("") }
    var shiftEnd by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("https://via.placeholder.com/150") }
    var isActive by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AppDialog(
        onDismiss = onDismiss,
        icon = Icons.Filled.PersonAdd,
        accent = Color(0xFF3B82F6),
        title = "Agregar nuevo trabajador",
        subtitle = "Registro con PIN de 4 dígitos",
        confirmLabel = "Guardar",
        confirmIcon = Icons.Filled.Save,
        onConfirm = {
            val rate = hourlyRateText.toDoubleOrNull()
            val hours = maxDailyHoursText.toIntOrNull()
            when {
                name.isBlank() -> errorText = "Escribe el nombre del trabajador."
                id.isBlank() -> errorText = "Escribe el ID del trabajador."
                pin.length != 4 -> errorText = "El PIN debe tener 4 dígitos."
                rate == null -> errorText = "Ingresa una tarifa válida."
                hours == null -> errorText = "Ingresa las horas máximas válidas."
                facility.isBlank() -> errorText = "Escribe la facility."
                shiftStart.isBlank() || shiftEnd.isBlank() -> errorText = "Indica inicio y fin del turno."
                else -> {
                    onSave(
                        Worker(
                            id = id.trim(),
                            docId = "",
                            name = name.trim(),
                            pin = pin,
                            photoUrl = photoUrl.trim().ifEmpty { "https://via.placeholder.com/150" },
                            hourlyRate = rate,
                            maxDailyHours = hours,
                            isActive = isActive,
                            shiftStart = shiftStart.trim(),
                            shiftEnd = shiftEnd.trim(),
                            facility = facility.trim()
                        )
                    )
                }
            }
        },
        contentMaxHeight = 470.dp
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("ID (ej. 001)") },
                leadingIcon = { Icon(Icons.Filled.Tag, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = { Text("PIN (4 dígitos)") },
                leadingIcon = { Icon(Icons.Filled.Password, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = hourlyRateText,
                onValueChange = { hourlyRateText = it },
                label = { Text("Tarifa/hora (14.5)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxDailyHoursText,
                onValueChange = { maxDailyHoursText = it },
                label = { Text("Horas máx. (8)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = facility,
            onValueChange = { facility = it },
            label = { Text("Facility (ej. Oficina de afuera)") },
            leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = shiftStart,
                onValueChange = { shiftStart = it },
                label = { Text("Inicio (HH:mm)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = shiftEnd,
                onValueChange = { shiftEnd = it },
                label = { Text("Fin (HH:mm)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Foto + estado con fondo tintado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Emerald50)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = photoUrl,
                onValueChange = { photoUrl = it },
                label = { Text("Foto (URL)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Activo",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gray700
                )
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Emerald500)
                )
            }
        }

        errorText?.let { DialogErrorHint(it) }
    }
}

// ===== DIÁLOGO DETALLE DE TRABAJADOR =====
@Composable
fun WorkerDetailDialog(
    detail: WorkerDetail,
    onDismiss: () -> Unit,
    onCompleteTask: (String) -> Unit = {}
) {
    val worker = detail.worker
    val now = System.currentTimeMillis()

    AppDialog(
        onDismiss = onDismiss,
        icon = Icons.Filled.Badge,
        accent = Emerald500,
        title = worker.name,
        subtitle = "#${worker.id} • ${worker.facility}",
        contentMaxHeight = 520.dp
    ) {
        // Chips con datos clave
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailChip(label = "Tarifa", value = "S/.${String.format("%.2f", worker.hourlyRate)}/h")
            DetailChip(label = "Turno", value = "${worker.shiftStart} - ${worker.shiftEnd}")
            DetailChip(label = "Horas", value = "${worker.maxDailyHours}h")
        }

        if (detail.overdueCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Red50)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Red500,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${detail.overdueCount} pendiente(s) vencido(s) — requiere atención",
                    color = Red500,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Sección: en curso
        DialogSectionHeader(
            icon = Icons.Filled.DirectionsRun,
            title = "Actividades realizando (${detail.inProgressTasks.size})",
            tint = Emerald600
        )

        if (detail.inProgressTasks.isEmpty()) {
            EmptyHint("Sin actividades en proceso")
        } else {
            detail.inProgressTasks.forEach { task ->
                ActivityRow(
                    title = task.title,
                    subtitle = "${formatMinutes(task.elapsedMinutes)} / ${formatMinutes(task.estimatedMinutes)}",
                    type = ActivityType.IN_PROGRESS
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Sección: pendientes
        DialogSectionHeader(
            icon = Icons.Filled.PendingActions,
            title = "Pendientes (${detail.pendingTasks.size})",
            tint = Color(0xFFF59E0B)
        )

        if (detail.pendingTasks.isEmpty()) {
            EmptyHint("Sin pendientes asignados")
        } else {
            detail.pendingTasks.forEach { task ->
                PendingTaskRow(
                    title = task.title,
                    description = task.description,
                    deadline = task.deadline,
                    isOverdue = task.deadline in 1..now,
                    onComplete = { onCompleteTask(task.id) }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/** Encabezado de sección con icono tintado dentro de los diálogos. */
@Composable
private fun DialogSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
    }
}

private enum class ActivityType { IN_PROGRESS, PENDING }

@Composable
private fun ActivityRow(title: String, subtitle: String, type: ActivityType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (type == ActivityType.IN_PROGRESS) Amber50 else Emerald50
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (type == ActivityType.IN_PROGRESS) Icons.Filled.Timer else Icons.Filled.Schedule,
                contentDescription = null,
                tint = if (type == ActivityType.IN_PROGRESS) Color(0xFFD97706) else Emerald600,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Gray900, maxLines = 2)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Gray500)
            }
        }
    }
}

@Composable
private fun PendingTaskRow(
    title: String,
    description: String,
    deadline: Long,
    isOverdue: Boolean,
    onComplete: () -> Unit
) {
    val deadlineText = if (deadline > 0) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(deadline))
    } else "Sin fecha"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) Red50 else Emerald50
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Gray900, maxLines = 2)
                if (description.isNotBlank()) {
                    Text(description, style = MaterialTheme.typography.bodySmall, color = Gray500, maxLines = 2)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = if (isOverdue) Red500 else Emerald600,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOverdue) "Vencido: $deadlineText" else "Máx. $deadlineText",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) Red500 else Emerald700,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            TextButton(onClick = onComplete) {
                Text(
                    text = "Completar",
                    color = Emerald600,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun RowScope.DetailChip(label: String, value: String) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Gray100),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Gray500)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Gray900, maxLines = 1)
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = Gray400,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

// =====================================================================
// SECCIONES DEL MENÚ (Actividades / Trabajadores / Reportes)
// =====================================================================

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Gray200)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = Gray500)
        }
    }
}

@Composable
private fun TaskStatusChip(isInProgress: Boolean) {
    val bg = if (isInProgress) Color(0xFFF59E0B).copy(alpha = 0.15f) else Emerald500.copy(alpha = 0.15f)
    val fg = if (isInProgress) Color(0xFFD97706) else Emerald700
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (isInProgress) "En proceso" else "Pendiente",
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ShiftStatusBadge(onShift: Boolean) {
    val color = if (onShift) Color(0xFF3B82F6) else Gray500
    val background = if (onShift) Color(0xFF3B82F6).copy(alpha = 0.12f) else Gray100
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (onShift) "En turno" else "Fuera de turno",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// ===== SECCIÓN: ACTIVIDADES Y PENDIENTES =====
@Composable
private fun ActivitiesSection(
    uiState: AdminDashboardUiState,
    onShowAddPending: () -> Unit,
    onShowAllPending: () -> Unit,
    onCompletePendingTask: (taskId: String, workerId: String) -> Unit,
    onShowEditPending: (PendingTask) -> Unit,
    onDeletePendingTask: (String) -> Unit,
    onWorkerSelected: (String) -> Unit
) {
    val now = System.currentTimeMillis()
    val workersMap = remember(uiState.workers) {
        uiState.workers.associateBy { it.docId.ifEmpty { it.id } }
    }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var taskPendingDelete by remember { mutableStateOf<PendingTask?>(null) }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SectionHeading("Actividades y pendientes", "Agrega, edita, completa o elimina tareas")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onShowAddPending,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Icon(Icons.Filled.AddTask, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Agregar", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onShowAllPending,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
            ) {
                Icon(Icons.Filled.PendingActions, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ver todas", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Pendientes (${uiState.pendingTasks.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.pendingTasks.isEmpty()) {
            EmptyHint("No hay actividades pendientes registradas")
        } else {
            uiState.pendingTasks.forEach { task ->
                val workerName = workersMap[task.workerId]?.name
                    ?: task.workerId.ifBlank { "Sin asignar" }
                val isOverdue = task.deadline in 1..now
                val isInProgress = task.status == PendingTaskStatus.IN_PROGRESS
                val accent = when {
                    isOverdue -> Red500
                    isInProgress -> Color(0xFFF59E0B)
                    else -> Emerald500
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isOverdue -> Red50
                            isInProgress -> Amber50
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray900,
                                maxLines = 2,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TaskStatusChip(isInProgress)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Asignado a $workerName",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gray500
                        )
                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray700,
                                maxLines = 2
                            )
                        }
                        if (task.deadline > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = if (isOverdue) Red500 else Emerald600,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = (if (isOverdue) "Vencido: " else "Máx. ") +
                                        dateFormat.format(Date(task.deadline)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOverdue) Red500 else Gray500,
                                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { onShowEditPending(task) }) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            TextButton(onClick = { taskPendingDelete = task }) {
                                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Eliminar", color = Red500, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            TextButton(onClick = { onCompletePendingTask(task.id, task.workerId) }) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Completar", color = Emerald600, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    taskPendingDelete?.let { task ->
        AppDialog(
            onDismiss = { taskPendingDelete = null },
            icon = Icons.Filled.Delete,
            accent = Red500,
            title = "Eliminar actividad",
            subtitle = task.title,
            confirmLabel = "Eliminar",
            confirmIcon = Icons.Filled.Delete,
            onConfirm = {
                onDeletePendingTask(task.id)
                taskPendingDelete = null
            }
        ) {
            Text(
                text = "¿Seguro que deseas eliminar esta actividad? Esta acción no se puede deshacer.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700
            )
        }
    }
}

// ===== SECCIÓN: TRABAJADORES =====
@Composable
private fun WorkersSection(
    uiState: AdminDashboardUiState,
    onShowAddWorker: () -> Unit,
    onShowDeleteWorker: () -> Unit,
    onDeleteWorker: (String) -> Unit,
    onWorkerSelected: (String) -> Unit
) {
    val onShiftIds = remember(uiState.activeWorkers) {
        uiState.activeWorkers.map { it.workerId }.toSet()
    }
    var workerPendingDelete by remember { mutableStateOf<Worker?>(null) }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SectionHeading("Trabajadores", "Plantilla registrada y estado de turno")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onShowAddWorker,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Agregar", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onShowDeleteWorker,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500),
                border = BorderStroke(1.dp, Red500.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Filled.PersonRemove, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Eliminar", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniStatCard("Registrados", "${uiState.workers.size}", Emerald600, Modifier.weight(1f))
            MiniStatCard("En turno", "${uiState.activeWorkers.size}", Color(0xFF3B82F6), Modifier.weight(1f))
            MiniStatCard(
                "Fuera",
                "${(uiState.workers.size - uiState.activeWorkers.size).coerceAtLeast(0)}",
                Gray500,
                Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (uiState.workers.isEmpty()) {
            EmptyHint("No hay trabajadores registrados")
        } else {
            uiState.workers.forEach { worker ->
                val docId = worker.docId.ifEmpty { worker.id }
                val onShift = docId in onShiftIds || worker.id in onShiftIds

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWorkerSelected(docId) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(
                        1.dp,
                        if (onShift) Color(0xFF3B82F6).copy(alpha = 0.35f) else Gray200
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(
                            icon = Icons.Filled.Person,
                            tint = if (onShift) Color(0xFF3B82F6) else Gray500,
                            size = 42.dp,
                            iconSize = 20.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = worker.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray900,
                                maxLines = 1
                            )
                            Text(
                                text = "#${worker.id} • ${worker.facility.ifEmpty { "Sin facility" }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ShiftStatusBadge(onShift)
                                Spacer(modifier = Modifier.width(6.dp))
                                WorkerStatusBadge(isActive = worker.isActive)
                            }
                        }
                        IconButton(onClick = { workerPendingDelete = worker }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Eliminar",
                                tint = Red500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    workerPendingDelete?.let { worker ->
        AppDialog(
            onDismiss = { workerPendingDelete = null },
            icon = Icons.Filled.PersonRemove,
            accent = Red500,
            title = "Eliminar trabajador",
            subtitle = worker.name,
            confirmLabel = "Eliminar",
            confirmIcon = Icons.Filled.Delete,
            onConfirm = {
                onDeleteWorker(worker.docId.ifEmpty { worker.id })
                workerPendingDelete = null
            }
        ) {
            Text(
                text = "Se cerrarán sus turnos abiertos y se eliminará el registro. Esta acción no se puede deshacer.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700
            )
        }
    }
}

// ===== SECCIÓN: REPORTES =====
@Composable
private fun ActivityNotificationsCard(notifications: List<AdminNotification>) {
    val timeFormat = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Gray200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Filled.NotificationsActive,
                    tint = Emerald600,
                    size = 36.dp,
                    iconSize = 18.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notificaciones de actividades",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Text(
                        text = "Avisos cuando un trabajador completa una actividad",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500
                    )
                }
                if (notifications.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Emerald50)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${notifications.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Emerald600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (notifications.isEmpty()) {
                EmptyHint("Sin actividades completadas por ahora")
            } else {
                notifications.take(5).forEach { notification ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(
                            icon = Icons.Filled.CheckCircle,
                            tint = Emerald500,
                            size = 30.dp,
                            iconSize = 16.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notification.workerName.ifBlank { "Trabajador" },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray900,
                                maxLines = 1
                            )
                            Text(
                                text = notification.taskTitle.ifBlank { "Actividad" },
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500,
                                maxLines = 1
                            )
                        }
                        if (notification.timestamp > 0) {
                            Text(
                                text = timeFormat.format(Date(notification.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
@Composable
private fun ReportsSection(uiState: AdminDashboardUiState, onShowDailyClosing: () -> Unit) {
    val now = System.currentTimeMillis()
    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    val pendingCount = uiState.pendingTasks.count { it.status == PendingTaskStatus.PENDING }
    val inProgressCount = uiState.pendingTasks.count { it.status == PendingTaskStatus.IN_PROGRESS }
    val overdueCount = uiState.pendingTasks.count { it.deadline in 1..now }
    val totalTasks = uiState.pendingTasks.size.coerceAtLeast(1)

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SectionHeading("Panel analítico", "Resumen operativo del día")

        ActivityNotificationsCard(uiState.taskNotifications)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onShowDailyClosing,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
        ) {
            Icon(
                Icons.Filled.EventAvailable,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cierre del día", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiCard(
                label = "Trabajadores activos",
                value = "${uiState.stats.totalActiveWorkers}",
                modifier = Modifier.weight(1f),
                valueColor = Emerald600,
                icon = Icons.Filled.Groups,
                accent = Emerald500,
                detail = "${uiState.activeWorkers.size} en turno ahora"
            )
            KpiCard(
                label = "Horas hoy",
                value = "%.1f".format(uiState.stats.totalHoursLogged),
                modifier = Modifier.weight(1f),
                valueColor = Color(0xFF3B82F6),
                icon = Icons.Filled.Schedule,
                accent = Color(0xFF3B82F6),
                detail = "registradas en el día"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiCard(
                label = "Planilla estimada",
                value = "S/ %.2f".format(uiState.stats.totalPayrollToday),
                modifier = Modifier.weight(1f),
                valueColor = Emerald700,
                icon = Icons.Filled.Payments,
                accent = Emerald500,
                detail = "pago acumulado hoy"
            )
            KpiCard(
                label = "Reportes recibidos",
                value = "${uiState.activityReports.size}",
                modifier = Modifier.weight(1f),
                valueColor = Color(0xFFF59E0B),
                icon = Icons.Filled.Assessment,
                accent = Color(0xFFF59E0B),
                detail = if (overdueCount > 0) "$overdueCount vencido(s)" else "al día"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta de distribución de actividades
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp),
            border = BorderStroke(1.dp, Gray200)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Distribución de actividades",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(14.dp))

                ReportBar("Pendientes", pendingCount, totalTasks, Emerald500)
                Spacer(modifier = Modifier.height(12.dp))
                ReportBar("En proceso", inProgressCount, totalTasks, Color(0xFFF59E0B))
                Spacer(modifier = Modifier.height(12.dp))
                ReportBar(
                    "Vencidas",
                    overdueCount,
                    totalTasks,
                    Red500
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Reporte de actividades diarias (${uiState.activityReports.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.activityReports.isEmpty()) {
            EmptyHint("Aún no hay movimientos reportados por los trabajadores")
        } else {
            uiState.activityReports.forEach { report ->
                val isCompleted = report.status == "COMPLETED"
                val accent = if (isCompleted) Emerald500 else Color(0xFFF59E0B)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) Emerald50 else Amber50
                    ),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(
                            icon = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Autorenew,
                            tint = accent,
                            size = 36.dp,
                            iconSize = 18.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = report.taskTitle.ifBlank { "Actividad" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray900,
                                maxLines = 1
                            )
                            Text(
                                text = (if (isCompleted) "Completada" else "En proceso") +
                                    " • ${report.workerName.ifBlank { "Trabajador" }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                            if (report.message.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${report.message}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray700,
                                    maxLines = 3
                                )
                            }
                            if (report.timestamp > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dateFormat.format(Date(report.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray500
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

private data class WorkerDaily(
    val name: String,
    val minutes: Int,
    val pay: Double,
    val shifts: Int
)

// ===== DIÁLOGO: CIERRE DEL DÍA =====
@Composable
private fun DailyClosingDialog(
    shifts: List<AttendanceRecord>,
    reports: List<ActivityReport>,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfDay = cal.timeInMillis

    val todayReports = reports.filter { it.timestamp >= startOfDay }
    val doneReports = todayReports.filter { it.status == "COMPLETED" }

    val totalMinutes = shifts.sumOf { it.elapsedMinutes }
    val totalPay = shifts.sumOf { it.currentPay }
    val openShifts = shifts.count { it.clockOutTime == null }

    // Detalle por trabajador: agrupa los turnos del día y suma horas + monto
    val perWorker = shifts
        .groupBy { it.workerId }
        .map { (_, recs) ->
            WorkerDaily(
                name = recs.firstOrNull()?.workerName?.ifBlank { "Trabajador" } ?: "Trabajador",
                minutes = recs.sumOf { it.elapsedMinutes },
                pay = recs.sumOf { it.currentPay },
                shifts = recs.size
            )
        }
        .sortedByDescending { it.pay }

    AppDialog(
        onDismiss = onDismiss,
        icon = Icons.Filled.EventAvailable,
        accent = Emerald600,
        title = "Cierre del día",
        subtitle = dateFormat.format(Date()),
        contentMaxHeight = 520.dp
    ) {
        // ===== Totales del día =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClosingStat(
                label = "Horas trabajadas",
                value = formatMinutes(totalMinutes),
                detail = if (openShifts > 0) "$openShifts turno(s) abierto(s)" else "Turnos cerrados",
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
            ClosingStat(
                label = "Monto a pagar",
                value = "S/ %.2f".format(totalPay),
                detail = "Total del día",
                color = Emerald700,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClosingStat(
                label = "Actividades",
                value = "${doneReports.size}",
                detail = "realizadas hoy",
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
            ClosingStat(
                label = "Trabajadores",
                value = "${perWorker.size}",
                detail = "con turno hoy",
                color = Emerald600,
                modifier = Modifier.weight(1f)
            )
        }

        // ===== Detalle por trabajador =====
        Text(
            text = "Detalle por trabajador",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
        if (perWorker.isEmpty()) {
            EmptyHint("Sin turnos registrados hoy")
        } else {
            perWorker.forEach { w ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, Gray200)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircleIconBadge(
                            icon = Icons.Filled.Person,
                            tint = Emerald600,
                            size = 36.dp,
                            iconSize = 18.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = w.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray900,
                                maxLines = 1
                            )
                            Text(
                                text = "${formatMinutes(w.minutes)} • ${w.shifts} turno(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500
                            )
                        }
                        Text(
                            text = "S/ %.2f".format(w.pay),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Emerald700
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ===== Actividades realizadas =====
        Text(
            text = "Actividades realizadas hoy",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
        if (doneReports.isEmpty()) {
            EmptyHint("Sin actividades completadas hoy")
        } else {
            doneReports.forEach { report ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(
                        icon = Icons.Filled.CheckCircle,
                        tint = Emerald500,
                        size = 30.dp,
                        iconSize = 16.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = report.taskTitle.ifBlank { "Actividad" },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray900,
                            maxLines = 1
                        )
                        Text(
                            text = report.workerName.ifBlank { "Trabajador" },
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray500
                        )
                    }
                    if (report.timestamp > 0) {
                        Text(
                            text = timeFormat.format(Date(report.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray500
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ClosingStat(
    label: String,
    value: String,
    detail: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray900
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = Gray500
            )
        }
    }
}

@Composable
private fun ReportBar(label: String, count: Int, total: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Gray700,
            modifier = Modifier.width(90.dp)
        )
        ProgressBar(
            progress = count.toFloat() / total.toFloat(),
            modifier = Modifier.weight(1f),
            barColor = color
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
    }
}

// ===== DIÁLOGO EDITAR PENDIENTE =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPendingTaskDialog(
    task: PendingTask,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, deadline: Long) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var deadline by remember { mutableStateOf(if (task.deadline > 0) task.deadline else null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val deadlineText = deadline?.let {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
    } ?: "Seleccionar fecha"

    AppDialog(
        onDismiss = onDismiss,
        icon = Icons.Filled.Edit,
        accent = Color(0xFF3B82F6),
        title = "Editar actividad",
        subtitle = "Actualiza los datos de la tarea",
        confirmLabel = "Guardar",
        confirmIcon = Icons.Filled.Save,
        onConfirm = {
            when {
                title.isBlank() -> errorText = "Escribe el título de la actividad."
                deadline == null -> errorText = "Selecciona la fecha máxima de realización."
                else -> onSave(title.trim(), description.trim(), deadline!!)
            }
        },
        contentMaxHeight = 440.dp
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título de la actividad") },
            leadingIcon = { Icon(Icons.Filled.EditNote, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción (opcional)") },
            leadingIcon = { Icon(Icons.Filled.Notes, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Emerald50)
                .border(1.dp, Emerald500.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .clickable { showDatePicker = true }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(
                icon = Icons.Filled.Event,
                tint = Emerald600,
                size = 34.dp,
                iconSize = 16.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Día máximo de realización",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500
                )
                Text(
                    text = deadlineText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Emerald700
                )
            }
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = Emerald600,
                modifier = Modifier.size(20.dp)
            )
        }

        errorText?.let { DialogErrorHint(it) }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = task.deadline.takeIf { it > 0 } ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = millis
                            cal.set(Calendar.HOUR_OF_DAY, 23)
                            cal.set(Calendar.MINUTE, 59)
                            cal.set(Calendar.SECOND, 59)
                            deadline = cal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar", color = Emerald600, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = Gray500)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ===== PREVIEW =====
@Preview(showBackground = true, showSystemUi = true, widthDp = 393, heightDp = 852)
@Composable
fun AdminDashboardPreview() {
    MaterialTheme {
        Surface(color = Gray50) {
            val previewState = AdminDashboardUiState(
                stats = AdminStats(
                    totalActiveWorkers = 12,
                    totalPayrollToday = 14909.52,
                    totalHoursLogged = 65190.70,
                    avgRate = 16.00,
                    overtime = "2:14:30",
                    pendingApprovals = 3
                ),
                activeWorkers = listOf(
                    com.example.ctpa.domain.model.AttendanceRecord(
                        id = "087",
                        workerId = "087",
                        workerName = "Mario Silva",
                        elapsedMinutes = 272,
                        currentPay = 72.53,
                        location = "Sector 7B - Brake Pads",
                        status = com.example.ctpa.domain.model.WorkerStatus.ACTIVE
                    ),
                    com.example.ctpa.domain.model.AttendanceRecord(
                        id = "091",
                        workerId = "091",
                        workerName = "Elvira Rios",
                        elapsedMinutes = 375,
                        currentPay = 93.75,
                        location = "Central Warehouse Unit B",
                        status = com.example.ctpa.domain.model.WorkerStatus.ON_BREAK
                    )
                ),
                workers = listOf(
                    Worker(id = "001", name = "Joshue Jesus", hourlyRate = 14.5, isActive = true, facility = "Oficina de afuera", docId = "worker_104")
                ),
                overtimeAlerts = listOf(
                    com.example.ctpa.domain.model.AttendanceRecord(
                        id = "104",
                        workerId = "104",
                        workerName = "Carlos Rodriguez",
                        elapsedMinutes = 496,
                        currentPay = 132.26,
                        location = "Sector 7 Plant",
                        status = com.example.ctpa.domain.model.WorkerStatus.ACTIVE,
                        isOvertime = true,
                        overtimeMinutes = 16
                    )
                )
            )

            AdminDashboardContent(uiState = previewState)
        }
    }
}