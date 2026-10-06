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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
            val workersSnapshot = firestore.collection("workers")
                .get()
                .await()

            val workersMap = workersSnapshot.documents.mapNotNull { mapWorker(it) }
                .associateBy { it.docId.ifEmpty { it.id } }

            val attendanceSnapshot = firestore.collection("attendance")
                .whereEqualTo("clockOutTime", null)
                .get()
                .await()

            val now = System.currentTimeMillis()
            var totalHours = 0.0
            var totalPayroll = 0.0
            var totalOvertimeMinutes = 0
            var pendingApprovalsCount = 0

            attendanceSnapshot.documents.forEach { doc ->
                val workerId = doc.getString("workerId") ?: ""
                val clockIn = doc.getLong("clockInTime") ?: now
                val elapsedMins = maxOf(0, ((now - clockIn) / 60000).toInt())
                val worker = workersMap[workerId] ?: workersMap.values.find { it.id == workerId }
                val rate = worker?.hourlyRate ?: 0.0
                val maxMins = (worker?.maxDailyHours ?: 8) * 60

                val hours = elapsedMins / 60.0
                totalHours += hours
                totalPayroll += hours * rate

                if (elapsedMins > maxMins) {
                    val ot = elapsedMins - maxMins
                    totalOvertimeMinutes += ot
                    pendingApprovalsCount++
                }
            }

            val activeWorkersCount = attendanceSnapshot.documents.size
            val avgRate = if (workersMap.isNotEmpty()) {
                workersMap.values.map { it.hourlyRate }.average()
            } else 0.0

            val otHours = totalOvertimeMinutes / 60
            val otMins = totalOvertimeMinutes % 60
            val overtimeStr = String.format("%02d:%02d:00", otHours, otMins)

            emit(
                AdminStats(
                    totalActiveWorkers = activeWorkersCount,
                    totalPayrollToday = totalPayroll,
                    totalHoursLogged = totalHours,
                    avgRate = avgRate,
                    overtime = overtimeStr,
                    pendingApprovals = pendingApprovalsCount
                )
            )
        } catch (e: Exception) {
            emit(AdminStats())
        }
    }

    override suspend fun getActiveWorkers(): Flow<List<AttendanceRecord>> = flow {
        try {
            val workersSnapshot = firestore.collection("workers")
                .get()
                .await()

            val workersMap = workersSnapshot.documents.mapNotNull { mapWorker(it) }
                .associateBy { it.docId.ifEmpty { it.id } }

            val attendanceSnapshot = firestore.collection("attendance")
                .whereEqualTo("clockOutTime", null)
                .get()
                .await()

            val now = System.currentTimeMillis()

            val records = attendanceSnapshot.documents.mapNotNull { doc ->
                val workerId = doc.getString("workerId") ?: ""
                val worker = workersMap[workerId] ?: workersMap.values.find { it.id == workerId }
                val clockIn = doc.getLong("clockInTime") ?: now
                val elapsedMins = maxOf(0, ((now - clockIn) / 60000).toInt())
                val rate = worker?.hourlyRate ?: 0.0
                val statusStr = doc.getString("status") ?: "ACTIVE"
                val status = if (statusStr == "ON_BREAK") WorkerStatus.ON_BREAK else WorkerStatus.ACTIVE

                AttendanceRecord(
                    id = doc.id,
                    workerId = worker?.docId ?: workerId,
                    workerName = worker?.name ?: doc.getString("workerName") ?: workerId,
                    clockInTime = clockIn,
                    elapsedMinutes = elapsedMins,
                    currentPay = (elapsedMins / 60.0) * rate,
                    location = worker?.facility ?: doc.getString("location") ?: "Main Plant",
                    status = status
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

            // Tareas pendientes
            val pendingSnapshot = firestore.collection("pending_tasks")
                .whereEqualTo("workerId", workerId)
                .whereEqualTo("status", PendingTaskStatus.PENDING.name)
                .get()
                .await()

            val pendingTasks = pendingSnapshot.documents.mapNotNull { doc ->
                val t = doc.toObject(PendingTask::class.java) ?: return@mapNotNull null
                t.copy(id = doc.id)
            }

            // Actividades en proceso
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

    override suspend fun getAllPendingTasks(): Flow<List<PendingTask>> = callbackFlow {
        val registration = firestore.collection("pending_tasks")
            .whereEqualTo("status", PendingTaskStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(PendingTask::class.java)?.copy(id = doc.id)
                    }
                    ?.sortedBy { it.deadline }
                    ?: emptyList()
                trySend(tasks)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getOvertimeAlerts(): Flow<List<AttendanceRecord>> = flow {
        try {
            val workersSnapshot = firestore.collection("workers")
                .get()
                .await()

            val workersMap = workersSnapshot.documents.mapNotNull { mapWorker(it) }
                .associateBy { it.docId.ifEmpty { it.id } }

            val attendanceSnapshot = firestore.collection("attendance")
                .whereEqualTo("clockOutTime", null)
                .get()
                .await()

            val now = System.currentTimeMillis()
            val alerts = mutableListOf<AttendanceRecord>()

            attendanceSnapshot.documents.forEach { doc ->
                val workerId = doc.getString("workerId") ?: ""
                val worker = workersMap[workerId] ?: workersMap.values.find { it.id == workerId }
                val clockIn = doc.getLong("clockInTime") ?: now
                val elapsedMins = maxOf(0, ((now - clockIn) / 60000).toInt())
                val maxMins = (worker?.maxDailyHours ?: 8) * 60

                if (elapsedMins > maxMins) {
                    val otMins = elapsedMins - maxMins
                    val rate = worker?.hourlyRate ?: 0.0
                    alerts.add(
                        AttendanceRecord(
                            id = doc.id,
                            workerId = worker?.docId ?: workerId,
                            workerName = worker?.name ?: doc.getString("workerName") ?: workerId,
                            clockInTime = clockIn,
                            elapsedMinutes = elapsedMins,
                            currentPay = (elapsedMins / 60.0) * rate,
                            location = worker?.facility ?: doc.getString("location") ?: "Main Plant",
                            status = WorkerStatus.ACTIVE,
                            isOvertime = true,
                            overtimeMinutes = otMins
                        )
                    )
                }
            }
            emit(alerts)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun approveOvertime(workerId: String) {
        firestore.collection("overtime_requests")
            .document(workerId)
            .set(mapOf("status" to "approved", "updatedAt" to System.currentTimeMillis()))
            .await()
    }

    override suspend fun rejectOvertime(workerId: String) {
        firestore.collection("overtime_requests")
            .document(workerId)
            .set(mapOf("status" to "rejected", "updatedAt" to System.currentTimeMillis()))
            .await()
    }

    override suspend fun forceClockOut(workerId: String) {
        val attendanceSnapshot = firestore.collection("attendance")
            .whereEqualTo("workerId", workerId)
            .whereEqualTo("clockOutTime", null)
            .get()
            .await()

        for (doc in attendanceSnapshot.documents) {
            firestore.collection("attendance")
                .document(doc.id)
                .update(
                    mapOf(
                        "clockOutTime" to System.currentTimeMillis(),
                        "status" to "CLOCKED_OUT"
                    )
                )
                .await()
        }
    }
}