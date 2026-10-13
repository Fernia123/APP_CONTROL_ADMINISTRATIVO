package com.example.ctpa.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ctpa.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_REMINDERS = "ctpa_worker_reminders"
        const val CHANNEL_ADMIN = "ctpa_admin_alerts"
        private const val REMINDER_NOTIFICATION_ID = 1001
    }

    private val counter = AtomicInteger(2000)

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Recordatorios de actividades",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Recuerda tus actividades durante el horario de trabajo"
                }
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ADMIN,
                    "Alertas para el administrador",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Avisos cuando un trabajador completa una actividad"
                }
            )
        }
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Recordatorio para el trabajador activo en horario laboral. */
    fun notifyActivityReminder(workerName: String, pendingTitles: List<String>) {
        if (pendingTitles.isEmpty()) return
        val summary = if (pendingTitles.size == 1) {
            pendingTitles.first()
        } else {
            "${pendingTitles.first()} y ${pendingTitles.size - 1} más"
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(
                "Recuerda tu actividad" + if (workerName.isNotBlank()) ", $workerName" else ""
            )
            .setContentText(summary)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Estás en horario de trabajo. Actividades pendientes: " +
                        pendingTitles.joinToString(", ")
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent())
            .setAutoCancel(true)
        post(REMINDER_NOTIFICATION_ID, builder)
    }

    /** Notifica al administrador que un trabajador terminó una actividad. */
    fun notifyAdminTaskCompleted(workerName: String, taskTitle: String) {
        val worker = workerName.ifBlank { "Un trabajador" }
        val task = taskTitle.ifBlank { "una actividad" }
        val builder = NotificationCompat.Builder(context, CHANNEL_ADMIN)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Actividad completada")
            .setContentText("$worker terminó: $task")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "El trabajador $worker marcó como completada la actividad \"$task\"."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent())
            .setAutoCancel(true)
        post(counter.incrementAndGet(), builder)
    }

    private fun post(id: Int, builder: NotificationCompat.Builder) {
        try {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        } catch (_: SecurityException) {
            // Permiso POST_NOTIFICATIONS no concedido: se ignora silenciosamente
        }
    }
}
