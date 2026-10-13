package com.example.ctpa.di

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    private const val TAG = "FirebaseModule"

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        // Conexión fluida: caché local persistente para lecturas instantáneas
        // y funcionamiento sin conexión. La app sigue sincronizando en segundo plano.
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            firestore.firestoreSettings = settings
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo activar la caché persistente de Firestore", e)
        }
        return firestore
    }
}
