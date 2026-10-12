package com.example.ctpa.data.repository


import com.example.ctpa.domain.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.example.ctpa.domain.model.AttendanceRecord
import com.example.ctpa.domain.model.PendingTask
import com.example.ctpa.domain.model.Worker
import com.example.ctpa.domain.model.WorkerStatus
import com.example.ctpa.domain.repository.LoginResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun getWorkers(): Flow<List<Worker>> = flow {
        try {
            // Obtener todos los trabajadores registrados en Firestore
            val snapshot = firestore.collection("workers")
                .get()
                .await()

            // Mapear documentos a objetos Worker
            val workers = snapshot.documents.mapNotNull { doc ->
                mapWorker(doc)
            }

            emit(workers)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getWorker(workerId: String): Flow<Worker?> = flow {
        try {
            val doc = firestore.collection("workers").document(workerId).get().await()
            if (doc.exists()) {
                emit(mapWorker(doc))
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    override suspend fun getPendingTasks(workerId: String): Flow<List<PendingTask>> = flow {
        try {
            val snapshot = firestore.collection("pending_tasks")
                .whereEqualTo("workerId", workerId)
                .get()
                .await()

            val tasks = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PendingTask::class.java)?.copy(id = doc.id)
            }
            emit(tasks)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getAttendanceHistory(workerId: String): Flow<List<AttendanceRecord>> = flow {
        try {
            val snapshot = firestore.collection("attendance")
                .whereEqualTo("workerId", workerId)
                .get()
                .await()

            val records = snapshot.documents.mapNotNull { doc ->
                val clockIn = doc.getLong("clockInTime") ?: return@mapNotNull null
                val clockOut = doc.getLong("clockOutTime")
                val rate = doc.getDouble("hourlyRate") ?: 0.0

                // Se conservan las horas y el pago guardados al cerrar el turno;
                // si no existen, se calculan de los timestamps.
                val elapsedMins = doc.getLong("elapsedMinutes")?.toInt()
                    ?: if (clockOut != null) maxOf(0, ((clockOut - clockIn) / 60000).toInt()) else 0
                val pay = doc.getDouble("totalPay")
                    ?: (elapsedMins / 60.0) * rate

                val statusStr = doc.getString("status") ?: "ACTIVE"
                val status = when (statusStr) {
                    "ON_BREAK" -> WorkerStatus.ON_BREAK
                    "CLOCKED_OUT" -> WorkerStatus.CLOCKED_OUT
                    else -> WorkerStatus.ACTIVE
                }

                AttendanceRecord(
                    id = doc.id,
                    workerId = workerId,
                    workerName = doc.getString("workerName") ?: "",
                    clockInTime = clockIn,
                    clockOutTime = clockOut,
                    elapsedMinutes = elapsedMins,
                    currentPay = pay,
                    location = doc.getString("location") ?: "",
                    status = status
                )
            }.sortedByDescending { it.clockInTime }

            emit(records)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun authenticate(pin: String): LoginResult {
        return try {
            // 1. Verificar si es la Clave Maestra (Admin)
            val adminDoc = firestore.collection("config")
                .document("admin_master")
                .get()
                .await()

            if (adminDoc.exists()) {
                val adminPin = adminDoc.getString("pin")
                if (adminPin == pin) {
                    return LoginResult.SuccessAdmin
                }
            }

            // 2. Verificar si es un PIN de Trabajador
            val workerSnapshot = firestore.collection("workers")
                .whereEqualTo("pin", pin)
                .limit(1)
                .get()
                .await()

            if (workerSnapshot.documents.isNotEmpty()) {
                val doc = workerSnapshot.documents.first()
                val worker = mapWorker(doc)
                if (worker != null) {
                    registerClockIn(worker)
                    return LoginResult.SuccessWorker(worker)
                }
            }

            // 3. Si no coincide con ninguno
            LoginResult.InvalidPin

        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "Error de conexión")
        }
    }

    private suspend fun registerClockIn(worker: Worker) {
        try {
            val workerKey = worker.docId.ifEmpty { worker.id }

            // Evitar duplicados: si ya tiene una entrada abierta, no registrar otra
            val existing = firestore.collection("attendance")
                .whereEqualTo("workerId", workerKey)
                .whereEqualTo("clockOutTime", null)
                .limit(1)
                .get()
                .await()

            if (existing.documents.isNotEmpty()) return

            firestore.collection("attendance")
                .add(
                    mapOf(
                        "workerId" to workerKey,
                        "workerName" to worker.name,
                        "clockInTime" to System.currentTimeMillis(),
                        "clockOutTime" to null,
                        "status" to "ACTIVE",
                        "location" to worker.facility,
                        // Se guarda la tarifa para conservar el cálculo del pago
                        "hourlyRate" to worker.hourlyRate,
                        "maxDailyHours" to worker.maxDailyHours,
                        "elapsedMinutes" to 0,
                        "totalPay" to 0.0
                    )
                )
                .await()
        } catch (e: Exception) {
            // No interrumpir el login si falla el registro de asistencia
        }
    }

    private fun mapWorker(doc: com.google.firebase.firestore.DocumentSnapshot): Worker? = try {
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
}