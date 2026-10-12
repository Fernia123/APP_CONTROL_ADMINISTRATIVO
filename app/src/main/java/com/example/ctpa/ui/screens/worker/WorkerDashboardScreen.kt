package com.example.ctpa.ui.screens.worker


import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ctpa.domain.model.Task
import com.example.ctpa.domain.model.TaskStatus
import com.example.ctpa.ui.components.*
import com.example.ctpa.ui.theme.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.tooling.preview.Preview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDashboardScreen(
    workerId: String,
    onBack: () -> Unit,
    viewModel: WorkerDashboardViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timeFormatted = viewModel.formatTime(uiState.elapsedSeconds)
    val isDark = themeViewModel.isDarkMode()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ShiftPulse", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("OPERACIONES DE CAMPO", style = MaterialTheme.typography.labelSmall, color = Gray500)
                    }
                },
                actions = {
                    IconButton(onClick = { themeViewModel.toggle(isDark) }) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = if (isDark) "Modo claro" else "Modo oscuro",
                            tint = Gray700
                        )
                    }
                    Text("Panel de tareas activas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        bottomBar = {
            // Bottom Navigation
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = null) },
                    label = { Text("Tareas") },
                    selected = uiState.selectedSection == 0,
                    onClick = { viewModel.onSectionSelected(0) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Checklist, contentDescription = null) },
                    label = { Text("Actividades") },
                    selected = uiState.selectedSection == 1,
                    onClick = { viewModel.onSectionSelected(1) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.History, contentDescription = null) },
                    label = { Text("Historial") },
                    selected = uiState.selectedSection == 2,
                    onClick = { viewModel.onSectionSelected(2) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("Perfil") },
                    selected = uiState.selectedSection == 3,
                    onClick = { viewModel.onSectionSelected(3) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Gray50)
        ) {
            when (uiState.selectedSection) {
                0 -> TasksSection(viewModel, uiState, timeFormatted)
                1 -> AssignedActivitiesSection(uiState)
                2 -> HistorySection(uiState)
                3 -> ProfileSection(
                    uiState,
                    isDark = isDark,
                    onDarkModeChange = themeViewModel::setDarkMode
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // Espacio para el bottom nav
        }
    }
}

@Composable
private fun TasksSection(
    viewModel: WorkerDashboardViewModel,
    uiState: WorkerDashboardUiState,
    timeFormatted: String
) {
    val totalCount = uiState.tasks.size
    val completedCount = uiState.tasks.count { it.status == TaskStatus.COMPLETED || it.status == TaskStatus.DONE }
    val activeCount = uiState.tasks.count { it.status == TaskStatus.ACTIVE || it.status == TaskStatus.IN_PROGRESS }

    var taskToUpdate by remember { mutableStateOf<Task?>(null) }
    var targetStatus by remember { mutableStateOf(TaskStatus.IN_PROGRESS) }

    Column {
        // 1. Timer Card
        TimerCard(
            timeElapsed = timeFormatted,
            startTime = uiState.startTime,
            targetTime = uiState.targetTime,
            isOnBreak = uiState.isOnBreak,
            onTakeBreak = { viewModel.toggleBreak() },
            facilityName = uiState.facility,
            modifier = Modifier.padding(16.dp)
        )

        // 2. Banner de Recordatorio (Log Sync)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Emerald50) // Verde muy claro
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Emerald500, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Recordatorio de sincronización", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Gray900)
                    Text("Actualiza tus tareas activas...", style = MaterialTheme.typography.bodySmall, color = Gray500)
                }
                TextButton(onClick = { viewModel.refreshLog() }) {
                    Text("Actualizar ahora", color = Emerald600, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        uiState.message?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald50)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Emerald500, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(message, style = MaterialTheme.typography.bodySmall, color = Gray700)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Sección de Tareas
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tareas asignadas de hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Gray900)
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Emerald50)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Verified,
                        contentDescription = null,
                        tint = Emerald600,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$completedCount of $totalCount Completed", style = MaterialTheme.typography.labelSmall, color = Emerald600, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs (All, Active, Done)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Gray100)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf("Todas ($totalCount)", "Activas ($activeCount)", "Completadas ($completedCount)")
                tabs.forEachIndexed { index, tab ->
                    val isSelected = uiState.selectedTab == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onTabSelected(index) },
                        label = { Text(tab, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.surface,
                            containerColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de Tareas Filtradas
            val filteredTasks = when (uiState.selectedTab) {
                1 -> uiState.tasks.filter { it.status == TaskStatus.ACTIVE || it.status == TaskStatus.IN_PROGRESS }
                2 -> uiState.tasks.filter { it.status == TaskStatus.COMPLETED || it.status == TaskStatus.DONE }
                else -> uiState.tasks
            }

            if (filteredTasks.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.isLoading) "Cargando tareas..." else "No hay tareas asignadas en esta categoría",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                }
            } else {
                filteredTasks.forEach { task ->
                    TaskItem(
                        task = task,
                        onMarkInProgress = {
                            taskToUpdate = task
                            targetStatus = TaskStatus.IN_PROGRESS
                        },
                        onMarkCompleted = {
                            taskToUpdate = task
                            targetStatus = TaskStatus.COMPLETED
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Botón CLOCK OUT / NUEVO TURNO
        Button(
            onClick = {
                if (uiState.isClockedOut) viewModel.restartShift() else viewModel.clockOut()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isClockedOut) Emerald500 else Color(0xFFEF4444)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (uiState.isClockedOut) "MARCAR ENTRADA / NUEVO TURNO" else "MARCAR SALIDA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (uiState.isClockedOut) "Reinicia el cronómetro para un nuevo turno" else "Verifica tu ubicación GPS y genera tu reporte de horas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Diálogo para marcar estado + dejar mensaje al admin
        taskToUpdate?.let { task ->
            TaskUpdateDialog(
                task = task,
                targetStatus = targetStatus,
                onDismiss = { taskToUpdate = null },
                onConfirm = { message ->
                    viewModel.updateTaskStatus(task, targetStatus, message)
                    taskToUpdate = null
                }
            )
        }
    }
}

@Composable
private fun TaskUpdateDialog(
    task: Task,
    targetStatus: TaskStatus,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var message by remember { mutableStateOf("") }
    val isCompleted = targetStatus == TaskStatus.COMPLETED

    AppDialog(
        onDismiss = onDismiss,
        icon = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Autorenew,
        accent = if (isCompleted) Emerald500 else Color(0xFFF59E0B),
        title = if (isCompleted) "Completar actividad" else "Marcar en proceso",
        subtitle = task.title,
        confirmLabel = if (isCompleted) "Completar" else "Guardar",
        confirmIcon = Icons.Filled.Save,
        onConfirm = { onConfirm(message) }
    ) {
        Text(
            text = "Deja un mensaje para tu administrador. Aparecerá en el reporte de actividades diarias.",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Mensaje para el admin (opcional)") },
            leadingIcon = { Icon(Icons.Filled.Chat, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
    }
}

@Composable
fun AssignedActivitiesSection(uiState: WorkerDashboardUiState) {
    val activeTasks = uiState.tasks.filter {
        it.status == TaskStatus.ACTIVE || it.status == TaskStatus.IN_PROGRESS
    }
    val doneTasks = uiState.tasks.filter {
        it.status == TaskStatus.COMPLETED || it.status == TaskStatus.DONE
    }

    SectionScaffold(
        icon = Icons.Filled.Checklist,
        title = "Actividades asignadas",
        subtitle = "Tus tareas activas y realizadas",
        accent = Emerald600
    ) {
        ActivityGroup(
            title = "Activas",
            count = activeTasks.size,
            accent = Emerald600,
            emptyText = "No tienes actividades activas",
            tasks = activeTasks
        )

        Spacer(modifier = Modifier.height(20.dp))

        ActivityGroup(
            title = "Realizadas",
            count = doneTasks.size,
            accent = Color(0xFF3B82F6),
            emptyText = "Aún no has completado actividades",
            tasks = doneTasks
        )
    }
}

@Composable
private fun ActivityGroup(
    title: String,
    count: Int,
    accent: Color,
    emptyText: String,
    tasks: List<Task>
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = accent
        )
    }
    Spacer(modifier = Modifier.height(10.dp))

    if (tasks.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(
                    icon = if (title == "Activas") Icons.Filled.DirectionsRun else Icons.Filled.DoneAll,
                    tint = accent,
                    size = 34.dp,
                    iconSize = 16.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
    } else {
        tasks.forEach { task ->
            TaskItem(task = task)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun HistorySection(uiState: WorkerDashboardUiState) {
    val totalMinutes = (uiState.weekHours * 60).toInt()
    val hoursText = "${totalMinutes / 60}h ${String.format("%02d", totalMinutes % 60)}m"
    val payText = "S/." + String.format("%.2f", uiState.weekPay)
    val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    SectionScaffold(
        icon = Icons.Filled.History,
        title = "Historial",
        subtitle = "Horas realizadas y monto a pagar por día",
        accent = Emerald600
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MiniStat(label = "Horas últimos 7 días", value = hoursText, modifier = Modifier.weight(1f))
            MiniStat(label = "Devengado 7 días", value = payText, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.dailyShifts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBadge(
                        icon = Icons.Filled.FactCheck,
                        tint = Emerald600,
                        size = 34.dp,
                        iconSize = 16.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Aún no hay turnos cerrados registrados",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
            }
        } else {
            uiState.dailyShifts.forEach { day ->
                DailyShiftCard(
                    day = day,
                    isToday = day.dateKey == todayKey,
                    overtimeStatus = uiState.overtimeStatus
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun DailyShiftCard(day: DailyShift, isToday: Boolean, overtimeStatus: String?) {
    val hoursText = "${day.elapsedMinutes / 60}h ${String.format("%02d", day.elapsedMinutes % 60)}m"
    val payText = "S/." + String.format("%.2f", day.pay)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Filled.CalendarMonth,
                    tint = if (isToday) Emerald600 else Color(0xFF3B82F6),
                    size = 34.dp,
                    iconSize = 16.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isToday) "${day.label} (Hoy)" else day.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Text(
                        text = "$hoursText trabajadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                    if (day.rate > 0.0) {
                        Text(
                            text = "Cobro por horas: S/.${String.format("%.2f", day.rate)}/h",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray500
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Monto a pagar", style = MaterialTheme.typography.labelSmall, color = Gray500)
                    Text(
                        text = payText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Emerald600
                    )
                }
            }

            if (day.isOvertime) {
                Spacer(modifier = Modifier.height(10.dp))
                OvertimeReviewBanner(
                    overtimeMinutes = day.overtimeMinutes,
                    status = overtimeStatus
                )
            }
        }
    }
}

private data class OvertimeTheme(
    val color: Color,
    val background: Color,
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

@Composable
private fun OvertimeReviewBanner(overtimeMinutes: Int, status: String?) {
    val otText = "${overtimeMinutes / 60}h ${String.format("%02d", overtimeMinutes % 60)}m"

    val theme = when (status) {
        "approved" -> OvertimeTheme(
            color = Emerald600,
            background = Emerald50,
            icon = Icons.Filled.CheckCircle,
            title = "Horas extra confirmadas",
            subtitle = "El administrador aprobó $otText adicionales"
        )
        "rejected" -> OvertimeTheme(
            color = Color(0xFFEF4444),
            background = Red50,
            icon = Icons.Filled.Cancel,
            title = "Horas extra no aprobadas",
            subtitle = "Se excedieron $otText sin autorización"
        )
        else -> OvertimeTheme(
            color = Color(0xFFD97706),
            background = Color(0xFFFEF3C7),
            icon = Icons.Filled.HourglassTop,
            title = "Horas extra en revisión",
            subtitle = "Excediste el horario por $otText — pendiente de confirmación del administrador"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(theme.background)
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = theme.icon,
            contentDescription = null,
            tint = theme.color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = theme.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = theme.color
            )
            Text(
                text = theme.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Gray700
            )
        }
    }
}

@Composable
fun ProfileSection(
    uiState: WorkerDashboardUiState,
    isDark: Boolean = false,
    onDarkModeChange: ((Boolean) -> Unit)? = null
) {
    val name = uiState.workerName.ifBlank { "Trabajador" }

    SectionScaffold(
        icon = Icons.Filled.Person,
        title = "Perfil",
        subtitle = "Datos del trabajador",
        accent = Color(0xFF7C3AED)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Emerald500, Emerald500.copy(alpha = 0.6f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(),
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Gray900)
                Text(uiState.facility.ifBlank { "Sede principal" }, style = MaterialTheme.typography.bodySmall, color = Gray500)
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniStat(label = "Objetivo turno", value = uiState.targetTime, modifier = Modifier.weight(1f))
                    MiniStat(label = "Inicio", value = uiState.startTime, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interruptor de modo oscuro
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Gray100)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBadge(
                        icon = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        tint = Color(0xFF7C3AED),
                        size = 34.dp,
                        iconSize = 16.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo oscuro",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray900
                        )
                        Text(
                            text = if (isDark) "Tema oscuro activo" else "Tema claro activo",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray500
                        )
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { onDarkModeChange?.invoke(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = Emerald500)
                    )
                }
            }
        }
    }
}

/** Contenedor de sección con badge de icono, título y contenido. */
@Composable
private fun SectionScaffold(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = icon, tint = accent, size = 36.dp, iconSize = 18.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Gray900)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Gray500)
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Gray100)
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Gray500, fontSize = 11.sp)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Gray900, fontSize = 16.sp)
    }
}