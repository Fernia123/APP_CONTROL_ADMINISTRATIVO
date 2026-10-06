package com.example.ctpa.data.repository


import com.example.ctpa.domain.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.example.ctpa.domain.model.PendingTask
import com.example.ctpa.domain.model.Worker
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
                    return LoginResult.SuccessWorker(worker)
                }
            }

            // 3. Si no coincide con ninguno
            LoginResult.InvalidPin

        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "Error de conexión")
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