package com.example.ctpa

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.ctpa.domain.model.AdminStats
import com.example.ctpa.domain.model.AttendanceRecord
import com.example.ctpa.domain.model.PendingTask
import com.example.ctpa.domain.model.Task
import com.example.ctpa.domain.model.TaskStatus
import com.example.ctpa.domain.model.Worker
import com.example.ctpa.domain.model.WorkerDetail
import com.example.ctpa.domain.model.WorkerStatus
import com.example.ctpa.ui.components.GpsRadar
import com.example.ctpa.ui.components.TaskItem
import com.example.ctpa.ui.components.TimerCard
import com.example.ctpa.ui.screens.admin.AdminDashboardContent
import com.example.ctpa.ui.screens.admin.AdminDashboardUiState
import com.example.ctpa.ui.screens.worker.HistorySection
import com.example.ctpa.ui.screens.worker.ProfileSection
import com.example.ctpa.ui.screens.worker.ScheduleSection
import com.example.ctpa.ui.screens.worker.WorkerDashboardUiState
import com.example.ctpa.ui.theme.CTPATheme
import com.example.ctpa.ui.theme.Gray50
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Captura pantallas reales (con datos de ejemplo) para verificar visualmente
 * la decoración de iconos y los estados "en vivo" de la UI.
 * Los PNG quedan en /sdcard/Android/data/com.example.ctpa/files/shots/
 */
@RunWith(AndroidJUnit4::class)
class ScreenshotCaptureTest {

    @get:Rule
    val compose = createComposeRule()

    private fun advance(ms: Long = 1500L) {
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeBy(ms)
    }

    private fun shot(name: String) {
        advance()
        saveShot(compose.onRoot().captureToImage().toPixelMap(), name)
    }

    /** Captura la ventana de diálogo (hay dos raíces cuando hay un Dialog abierto). */
    private fun shotDialog(name: String) {
        advance()
        saveShot(compose.onNode(isDialog()).captureToImage().toPixelMap(), name)
    }

    private fun saveShot(pixelMap: PixelMap, name: String) {
        val bitmap = Bitmap.createBitmap(pixelMap.width, pixelMap.height, Bitmap.Config.ARGB_8888)
        for (y in 0 until pixelMap.height) {
            for (x in 0 until pixelMap.width) {
                bitmap.setPixel(x, y, pixelMap[x, y].toArgb())
            }
        }
        val dir = InstrumentationRegistry.getInstrumentation()
            .targetContext.getExternalFilesDir("shots") ?: File("/sdcard/shots")
        dir.mkdirs()
        FileOutputStream(File(dir, "$name.png")).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    @Test
    fun adminDashboard() {
        compose.setContent {
            CTPATheme(isDark = false) {
                Surface(color = Gray50) {
                    AdminDashboardContent(uiState = adminState())
                }
            }
        }
        shot("admin-01-top")

        // Desplazamiento corto: alerta de overtime + trabajadores
        compose.onRoot().performTouchInput {
            swipe(
                start = Offset(width / 2f, height * 0.80f),
                end = Offset(width / 2f, height * 0.52f),
                durationMillis = 300
            )
        }
        shot("admin-02-alerts")

        compose.onRoot().performTouchInput { swipeUp() }
        shot("admin-03-radar")
    }

    @Test
    fun workerShiftAndTasks() {
        compose.setContent {
            CTPATheme(isDark = false) {
                Surface(color = Gray50) {
                    Column {
                        TimerCard(
                            timeElapsed = "03:42:15",
                            startTime = "07:04 AM",
                            targetTime = "8h 00m",
                            isOnBreak = false,
                            onTakeBreak = {},
                            facilityName = "Sector 7B - Brake Pads"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(modifier = Modifier.height(700.dp)) {
                            TaskItem(task = sampleTask(TaskStatus.ACTIVE, "Inspección de frenos", "Revisar pastillas y discos del eje delantero"))
                            Spacer(modifier = Modifier.height(10.dp))
                            TaskItem(task = sampleTask(TaskStatus.IN_PROGRESS, "Cambio de rodamientos", "Línea de ensamble 3", elapsed = 95, estimated = 150))
                            Spacer(modifier = Modifier.height(10.dp))
                            TaskItem(task = sampleTask(TaskStatus.COMPLETED, "Sellado de empaques", "Turno matutino", signed = true))
                            Spacer(modifier = Modifier.height(10.dp))
                            TaskItem(task = sampleTask(TaskStatus.DONE, "Limpieza de equipo", "Checklist semanal", photos = 3))
                        }
                    }
                }
            }
        }
        shot("worker-01-shift")
    }

    @Test
    fun workerSections() {
        val state = WorkerDashboardUiState(
            workerName = "Carlos Rodriguez",
            facility = "Sector 7 Plant",
            startTime = "07:04 AM",
            targetTime = "8h 00m"
        )
        compose.setContent {
            CTPATheme(isDark = false) {
                Surface(color = Gray50) {
                    Column {
                        ScheduleSection(state)
                        Spacer(modifier = Modifier.height(16.dp))
                        HistorySection()
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileSection(state)
                    }
                }
            }
        }
        shot("worker-02-sections")
    }

    @Test
    fun radarAlone() {
        compose.setContent {
            CTPATheme(isDark = false) {
                Surface(color = Gray50) {
                    Column(modifier = Modifier.height(420.dp)) {
                        GpsRadar(modifier = Modifier.height(400.dp))
                    }
                }
            }
        }
        shot("worker-03-radar")
    }

    // ===== VENTANAS EMERGENTES (claro + oscuro) =====

    @Test
    fun dialogAddPendingLight() {
        compose.setContent {
            CTPATheme(isDark = false) {
                Surface(color = Gray50) {
                    AdminDashboardContent(
                        uiState = adminState().copy(showAddPendingDialog = true)
                    )
                }
            }
        }
        shotDialog("dialog-01-pending-light")
    }

    @Test
    fun dialogAddPendingDark() {
        compose.setContent {
            CTPATheme(isDark = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AdminDashboardContent(
                        uiState = adminState().copy(showAddPendingDialog = true)
                    )
                }
            }
        }
        shotDialog("dialog-02-pending-dark")
    }

    @Test
    fun dialogAddWorkerDark() {
        compose.setContent {
            CTPATheme(isDark = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AdminDashboardContent(
                        uiState = adminState().copy(showAddWorkerDialog = true)
                    )
                }
            }
        }
        shotDialog("dialog-03-addworker-dark")
    }

    @Test
    fun dialogWorkerDetailDark() {
        compose.setContent {
            CTPATheme(isDark = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AdminDashboardContent(
                        uiState = adminState().copy(
                            selectedWorkerDetail = workerDetail(),
                            showWorkerDetail = true
                        )
                    )
                }
            }
        }
        shotDialog("dialog-04-detail-dark")
    }

    // ===== MODO OSCURO =====

    @Test
    fun darkModeAdminDashboard() {
        compose.setContent {
            CTPATheme(isDark = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AdminDashboardContent(uiState = adminState())
                }
            }
        }
        shot("dark-01-admin-top")

        compose.onRoot().performTouchInput {
            swipe(
                start = Offset(width / 2f, height * 0.80f),
                end = Offset(width / 2f, height * 0.52f),
                durationMillis = 300
            )
        }
        shot("dark-02-admin-alerts")
    }

    @Test
    fun darkModeWorkerSections() {
        val state = WorkerDashboardUiState(
            workerName = "Carlos Rodriguez",
            facility = "Sector 7 Plant",
            startTime = "07:04 AM",
            targetTime = "8h 00m"
        )
        compose.setContent {
            CTPATheme(isDark = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        ScheduleSection(state)
                        Spacer(modifier = Modifier.height(16.dp))
                        HistorySection()
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileSection(state, isDark = true)
                    }
                }
            }
        }
        shot("dark-03-worker-sections")
    }

    @Test
    fun darkModeTimerAndTasks() {
        compose.setContent {
            CTPATheme(isDark = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        TimerCard(
                            timeElapsed = "03:42:15",
                            startTime = "07:04 AM",
                            targetTime = "8h 00m",
                            isOnBreak = false,
                            onTakeBreak = {},
                            facilityName = "Sector 7B - Brake Pads"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(modifier = Modifier.height(560.dp)) {
                            TaskItem(task = sampleTask(TaskStatus.ACTIVE, "Inspección de frenos", "Revisar pastillas y discos"))
                            Spacer(modifier = Modifier.height(10.dp))
                            TaskItem(task = sampleTask(TaskStatus.IN_PROGRESS, "Cambio de rodamientos", "Línea de ensamble 3", elapsed = 95, estimated = 150))
                            Spacer(modifier = Modifier.height(10.dp))
                            TaskItem(task = sampleTask(TaskStatus.COMPLETED, "Sellado de empaques", "Turno matutino", signed = true))
                        }
                    }
                }
            }
        }
        shot("dark-04-timer-tasks")
    }

    private fun workerDetail() = WorkerDetail(
        worker = Worker(
            id = "087",
            docId = "worker_104",
            name = "Mario Silva",
            hourlyRate = 14.5,
            isActive = true,
            facility = "Sector 7B - Brake Pads",
            photoUrl = ""
        ),
        inProgressTasks = listOf(
            sampleTask(TaskStatus.IN_PROGRESS, "Cambio de rodamientos", "Línea de ensamble 3", elapsed = 95, estimated = 150)
        ),
        pendingTasks = listOf(
            PendingTask(
                id = "p1",
                workerId = "087",
                title = "Inspección de frenos",
                description = "Revisar pastillas y discos del eje delantero",
                deadline = System.currentTimeMillis() + 86_400_000L
            ),
            PendingTask(
                id = "p2",
                workerId = "087",
                title = "Reporte de calidad semanal",
                description = "",
                deadline = System.currentTimeMillis() - 3_600_000L
            )
        ),
        overdueCount = 1
    )

    private fun adminState() = AdminDashboardUiState(
        stats = AdminStats(
            totalActiveWorkers = 12,
            totalPayrollToday = 14909.52,
            totalHoursLogged = 65190.70,
            avgRate = 16.00,
            overtime = "2:14:30",
            pendingApprovals = 3
        ),
        activeWorkers = listOf(
            AttendanceRecord(
                id = "087",
                workerId = "087",
                workerName = "Mario Silva",
                elapsedMinutes = 272,
                currentPay = 72.53,
                location = "Sector 7B - Brake Pads",
                status = WorkerStatus.ACTIVE
            ),
            AttendanceRecord(
                id = "091",
                workerId = "091",
                workerName = "Elvira Rios",
                elapsedMinutes = 375,
                currentPay = 93.75,
                location = "Central Warehouse Unit B",
                status = WorkerStatus.ON_BREAK
            )
        ),
        workers = listOf(
            Worker(
                id = "001",
                name = "Joshue Jesus",
                hourlyRate = 14.5,
                isActive = true,
                facility = "Oficina de afuera",
                docId = "worker_104"
            )
        ),
        overtimeAlerts = listOf(
            AttendanceRecord(
                id = "104",
                workerId = "104",
                workerName = "Carlos Rodriguez",
                elapsedMinutes = 496,
                currentPay = 132.26,
                location = "Sector 7 Plant",
                status = WorkerStatus.ACTIVE,
                isOvertime = true,
                overtimeMinutes = 16
            )
        ),
        isLoading = false
    )

    private fun sampleTask(
        status: TaskStatus,
        title: String,
        description: String,
        elapsed: Int = 45,
        estimated: Int = 120,
        signed: Boolean = false,
        photos: Int = 0
    ) = Task(
        id = "t_$title",
        title = title,
        description = description,
        status = status,
        estimatedMinutes = estimated,
        elapsedMinutes = elapsed,
        signedBy = if (signed) "Supervisor López" else null,
        signedTime = if (signed) "12:35" else null,
        photoCount = photos,
        workerId = "087"
    )
}
