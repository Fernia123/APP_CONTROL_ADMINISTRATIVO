package com.example.ctpa.data.repository


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.DocumentSnapshot
import com.example.ctpa.domain.model.AdminStats
import com.example.ctpa.domain.model.AttendanceRecord
import com.example.ctpa.domain.model.PendingTask
import com.example.ctpa.domain.model.PendingTaskStatus
import com.example.ctpa.domain.model.Task
import com.example.ctpa.domain.model.TaskStatus
import com.example.ctpa.domain.model.Worker
import com.example.ctpa.domain.model.WorkerDetail
import com.example.ctpa.domain.model.WorkerStatus
import com.example.ctpa.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminRepository {

    override suspend fun getAdminStats(): Flow<AdminStats> = flow {
        try {
            val snapshot = firestore.collection("workers")
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val workers = snapshot.documents.mapNotNull { mapWorker(it) }
            val avgRate = if (workers.isNotEmpty()) {
                workers.map { it.hourlyRate }.average()
            } else 0.0

            emit(
                AdminStats(
                    totalActiveWorkers = workers.size,
                    totalPayrollToday = workers.sumOf { it.hourlyRate * it.maxDailyHours },
                    totalHoursLogged = workers.size * 8.0,
                    avgRate = avgRate,
                    overtime = "--:--:--",
                    pendingApprovals = 0
                )
            )
        } catch (e: Exception) {
            emit(AdminStats())
        }
    }

    override suspend fun getActiveWorkers(): Flow<List<AttendanceRecord>> = flow {
        try {
            val snapshot = firestore.collection("workers")
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val records = snapshot.documents.mapNotNull { doc ->
                val w = mapWorker(doc) ?: return@mapNotNull null
                AttendanceRecord(
                    id = doc.id,
                    workerId = doc.id,
                    workerName = w.name.ifEmpty { doc.id },
                    elapsedMinutes = 0,
                    currentPay = 0.0,
                    location = w.facility,
                    status = WorkerStatus.ACTIVE
                )
            }
            emit(records)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getWorkers(): Flow<List<Worker>> = flow {
        try {
            val snapshot = firestore.collection("workers").get().await()
            val workers = snapshot.documents.mapNotNull { mapWorker(it) }
            emit(workers)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun addWorker(worker: Worker) {
        val id = worker.id.ifBlank { worker.docId }
        val data = mapOf(
            "facility" to worker.facility,
            "hourlyRate" to worker.hourlyRate,
            "id" to id,
            "isActive" to worker.isActive,
            "maxDailyHours" to worker.maxDailyHours,
            "name" to worker.name,
            "photoUrl" to worker.photoUrl,
            "pin" to worker.pin,
            "shiftEnd" to worker.shiftEnd,
            "shiftStart" to worker.shiftStart
        )
        firestore.collection("workers")
            .add(data)
            .await()
    }

    private fun mapWorker(doc: DocumentSnapshot): Worker? = try {
        Worker(
            id = doc.getString("id") ?: "",
            docId = doc.id,
            name = doc.getString("name") ?: "",
            pin = doc.getString("pin") ?: "",
            photoUrl = doc.getString("photoUrl") ?: "",
            hourlyRate = doc.getDouble("hourlyRate") ?: 0.0,
            maxDailyHours = doc.getLong("maxDailyHours")?.toInt() ?: 8,
            isActive = doc.getBoolean("isActive") ?: false,
            shiftStart = doc.getString("shiftStart") ?: "",
            shiftEnd = doc.getString("shiftEnd") ?: "",
            facility = doc.getString("facility") ?: ""
        )
    } catch (e: Exception) {
        null
    }

    override suspend fun getWorkerDetail(workerId: String): Flow<WorkerDetail?> = flow {
        try {
            val workerDoc = firestore.collection("workers").document(workerId).get().await()
            val worker = mapWorker(workerDoc) ?: return@flow emit(null)

            // Tareas pendientes (pendientes por iniciar con fecha máxima)
            val pendingSnapshot = firestore.collection("pending_tasks")
                .whereEqualTo("workerId", workerId)
                .whereEqualTo("status", PendingTaskStatus.PENDING.name)
                .get()
                .await()

            val pendingTasks = pendingSnapshot.documents.mapNotNull { doc ->
                val t = doc.toObject(PendingTask::class.java) ?: return@mapNotNull null
                t.copy(id = doc.id)
            }

            // Actividades en proceso (in progress y activas)
            val inProgressSnapshot = firestore.collection("tasks")
                .whereEqualTo("workerId", workerId)
                .whereIn("status", listOf(TaskStatus.IN_PROGRESS.name, TaskStatus.ACTIVE.name))
                .get()
                .await()

            val inProgressTasks = inProgressSnapshot.documents.mapNotNull { doc ->
                val t = doc.toObject(Task::class.java) ?: return@mapNotNull null
                t.copy(id = doc.id)
            }

            val now = System.currentTimeMillis()
            val overdueCount = pendingTasks.count { it.deadline in 1..now }

            emit(
                WorkerDetail(
                    worker = worker,
                    inProgressTasks = inProgressTasks,
                    pendingTasks = pendingTasks,
                    overdueCount = overdueCount
                )
            )
        } catch (e: Exception) {
            emit(null)
        }
    }

    override suspend fun addPendingTask(
        workerId: String,
        title: String,
        description: String,
        deadline: Long
    ) {
        firestore.collection("pending_tasks")
            .add(
                mapOf(
                    "workerId" to workerId,
                    "title" to title,
                    "description" to description,
                    "deadline" to deadline,
                    "createdAt" to System.currentTimeMillis(),
                    "status" to PendingTaskStatus.PENDING.name
                )
            )
            .await()
    }

    override suspend fun completePendingTask(workerId: String, taskId: String) {
        firestore.collection("pending_tasks")
            .document(taskId)
            .update("status", PendingTaskStatus.COMPLETED.name)
            .await()
    }

    override suspend fun getOvertimeAlerts(): Flow<List<AttendanceRecord>> = flow {
        try {
            emit(
                listOf(
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
                )
            )
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun approveOvertime(workerId: String) {
        firestore.collection("overtime_requests")
            .document(workerId)
            .update("status", "approved")
            .await()
    }

    override suspend fun rejectOvertime(workerId: String) {
        firestore.collection("overtime_requests")
            .document(workerId)
            .update("status", "rejected")
            .await()
    }

    override suspend fun forceClockOut(workerId: String) {
        firestore.collection("attendance")
            .document(workerId)
            .update("clockOutTime", com.google.firebase.firestore.FieldValue.serverTimestamp())
            .await()
    }
}