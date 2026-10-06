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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ctpa.domain.model.TaskStatus
import com.example.ctpa.ui.components.*
import com.example.ctpa.ui.theme.*
import androidx.compose.foundation.layout.ColumnScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDashboardScreen(
    workerId: String,
    onBack: () -> Unit,
    viewModel: WorkerDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timeFormatted = viewModel.formatTime(uiState.elapsedSeconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ShiftPulse", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("FIELD OPERATIONS", style = MaterialTheme.typography.labelSmall, color = Gray500)
                    }
                },
                actions = {
                    Text("Active Tasks Dashboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Bottom Navigation
            NavigationBar(containerColor = White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = null) },
                    label = { Text("Tasks") },
                    selected = uiState.selectedSection == 0,
                    onClick = { viewModel.onSectionSelected(0) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                    label = { Text("Schedule") },
                    selected = uiState.selectedSection == 1,
                    onClick = { viewModel.onSectionSelected(1) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.History, contentDescription = null) },
                    label = { Text("History") },
                    selected = uiState.selectedSection == 2,
                    onClick = { viewModel.onSectionSelected(2) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("Profile") },
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
                1 -> ScheduleSection(uiState)
                2 -> HistorySection()
                3 -> ProfileSection(uiState)
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)) // Verde muy claro
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Emerald500, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Log sync reminder", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Gray900)
                    Text("Update your active tasks...", style = MaterialTheme.typography.bodySmall, color = Gray500)
                }
                TextButton(onClick = { viewModel.refreshLog() }) {
                    Text("Update Now", color = Emerald600, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
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
                Text("Today's Assigned Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Gray900)
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
                val tabs = listOf("All ($totalCount)", "Active ($activeCount)", "Done ($completedCount)")
                tabs.forEachIndexed { index, tab ->
                    val isSelected = uiState.selectedTab == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onTabSelected(index) },
                        label = { Text(tab, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = White,
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
                    colors = CardDefaults.cardColors(containerColor = White)
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
                    TaskItem(task = task)
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
                    text = if (uiState.isClockedOut) "CLOCK IN / INICIO DE NUEVO TURNO" else "CLOCK OUT / SALIDA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (uiState.isClockedOut) "Reinicia el cronómetro para un nuevo turno" else "Logs verified GPS coordinates & generates timesheet",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ScheduleSection(uiState: WorkerDashboardUiState) {
    val shifts = listOf(
        Triple("Lun 06 - Vie 10 Oct", "07:00 - 15:30", "Turno A • ${uiState.facility.ifBlank { "Main Facility" }}"),
        Triple("Sáb 11 Oct", "08:00 - 12:00", "Medio turno • mantenimiento"),
        Triple("Lun 13 - Vie 17 Oct", "07:00 - 15:30", "Turno A • confirmado")
    )

    SectionScaffold(
        icon = Icons.Filled.CalendarMonth,
        title = "Schedule",
        subtitle = "Próximos turnos asignados",
        accent = Color(0xFF3B82F6)
    ) {
        shifts.forEach { (days, hours, note) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBadge(
                        icon = Icons.Filled.Event,
                        tint = Color(0xFF3B82F6),
                        size = 34.dp,
                        iconSize = 16.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(days, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Gray900)
                        Text(note, style = MaterialTheme.typography.bodySmall, color = Gray500)
                    }
                    Text(hours, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun HistorySection() {
    val week = Triple("40h 15m", "$642.50", "5 de 5 turnos")

    SectionScaffold(
        icon = Icons.Filled.History,
        title = "History",
        subtitle = "Horas y turnos registrados",
        accent = Emerald600
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MiniStat(label = "Semana 41", value = week.first, modifier = Modifier.weight(1f))
            MiniStat(label = "Devengado", value = week.second, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text("Última semana", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Gray900)
                    Text("Firmado y sincronizado con Firebase", style = MaterialTheme.typography.bodySmall, color = Gray500)
                }
                Text(week.third, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Emerald600)
            }
        }
    }
}

@Composable
fun ProfileSection(uiState: WorkerDashboardUiState) {
    val name = uiState.workerName.ifBlank { "Trabajador" }

    SectionScaffold(
        icon = Icons.Filled.Person,
        title = "Profile",
        subtitle = "Datos del trabajador",
        accent = Color(0xFF7C3AED)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White)
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
                Text(uiState.facility.ifBlank { "Main Facility" }, style = MaterialTheme.typography.bodySmall, color = Gray500)
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniStat(label = "Objetivo turno", value = uiState.targetTime, modifier = Modifier.weight(1f))
                    MiniStat(label = "Inicio", value = uiState.startTime, modifier = Modifier.weight(1f))
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