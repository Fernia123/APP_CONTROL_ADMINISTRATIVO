package com.example.ctpa.ui.screens.admin


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.ctpa.domain.model.AdminStats
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
        onForceClockOut = viewModel::forceClockOut
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
    onForceClockOut: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ShiftPulse",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "FIELD OPERATIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray500
                        )
                    }
                },
                actions = {
                    Text(
                        text = "Overview",
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
        }
    ) { paddingValues ->
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
                    text = "Today, ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}",
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

            // ===== KPIs GRID =====
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        label = "Total Active Workers",
                        value = "${uiState.stats.totalActiveWorkers}",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Groups,
                        accent = Emerald500,
                        detail = "${uiState.activeWorkers.size} en turno ahora"
                    )
                    KpiCard(
                        label = "Total Payroll Today",
                        value = "$${String.format("%.2f", uiState.stats.totalPayrollToday)}",
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
                        label = "Total Hours Logged",
                        value = "${String.format("%.2f", uiState.stats.totalHoursLogged)} hrs",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.HourglassBottom,
                        accent = Color(0xFF3B82F6),
                        detail = "Sincronizado con Firebase"
                    )
                    KpiCard(
                        label = "Avg Rate",
                        value = "$${String.format("%.2f", uiState.stats.avgRate)}",
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
                        label = "Overtime",
                        value = uiState.stats.overtime,
                        modifier = Modifier.weight(1f),
                        valueColor = Color(0xFFF59E0B),
                        icon = Icons.Filled.MoreTime,
                        accent = Color(0xFFF59E0B),
                        detail = if (uiState.overtimeAlerts.isNotEmpty()) "${uiState.overtimeAlerts.size} alerta(s) activa(s)" else "Sin excesos hoy"
                    )
                    KpiCard(
                        label = "Pending Approvals",
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

            // ===== BOTÓN AGREGAR PENDIENTE =====
            Button(
                onClick = onShowAddPending,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Icon(Icons.Filled.AddTask, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Agregar Pendiente / Nueva Actividad",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ===== BOTÓN AGREGAR TRABAJADOR =====
            Button(
                onClick = onShowAddWorker,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald700Container)
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Agregar Nuevo Trabajador",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== ALERTAS DE OVERTIME =====
            if (uiState.overtimeAlerts.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Critical Alerts",
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
                        text = "Active Workers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${uiState.activeWorkers.size} Online",
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
                            text = "No active workers",
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

            Spacer(modifier = Modifier.height(24.dp))
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
}

// ===== DIÁLOGO AGREGAR PENDIENTE =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPendingTaskDialog(
    workers: List<Worker>,
    defaultWorkerId: String,
    onDismiss: () -> Unit,
    onSave: (workerId: String, title: String, description: String, deadline: Long) -> Unit
) {
    val availableWorkers = workers.filter { it.isActive }

    var selectedWorkerId by remember {
        mutableStateOf(
            defaultWorkerId.ifEmpty { availableWorkers.firstOrNull()?.docId ?: "" }
        )
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
                value = availableWorkers.firstOrNull { it.docId == selectedWorkerId }?.name
                    ?: "Selecciona un trabajador",
                onValueChange = {},
                readOnly = true,
                label = { Text("Trabajador") },
                leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = workerMenuExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = workerMenuExpanded,
                onDismissRequest = { workerMenuExpanded = false }
            ) {
                availableWorkers.forEach { worker ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(worker.name, fontWeight = FontWeight.Medium)
                                Text(
                                    "#${worker.id} • ${worker.facility}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray500
                                )
                            }
                        },
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
                    Text("OK", color = Emerald600, fontWeight = FontWeight.Bold)
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
            DetailChip(label = "Tasa", value = "$${String.format("%.2f", worker.hourlyRate)}/hr")
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