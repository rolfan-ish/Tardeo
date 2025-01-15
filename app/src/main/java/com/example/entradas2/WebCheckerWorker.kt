package com.example.webchecker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.entradas2.R
import org.jsoup.Jsoup
import java.lang.Exception

/**
 * Worker que realiza la tarea de comprobar una página web en segundo plano
 * para verificar si contiene una palabra específica y, si es así, envía
 * una notificación.
 *
 * <p>Esta clase utiliza `Jsoup` para realizar la conexión HTTP y extraer
 * el contenido de texto de una página web. La URL y la palabra a buscar
 * se almacenan en las preferencias compartidas de la aplicación
 * (`SharedPreferences`). Si la palabra se encuentra en el contenido de la
 * página, se envía una notificación al usuario.</p>
 *
 * @param appContext El contexto de la aplicación.
 * @param workerParams Parámetros adicionales proporcionados por WorkManager.
 */
class WebCheckerWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    /**
     * Método principal que realiza la tarea en segundo plano.
     *
     * <p>Obtiene la URL y la palabra a buscar desde `SharedPreferences`,
     * se conecta a la página web usando `Jsoup`, y verifica si la palabra
     * está presente en el contenido. Si encuentra la palabra, llama a
     * `sendNotification` para enviar una notificación.</p>
     *
     * @return Un objeto [Result] que indica si el trabajo fue exitoso o no.
     */
    override fun doWork(): Result {
        try {
            // Obtener la URL y la palabra desde las preferencias compartidas
            val sharedPreferences = applicationContext.getSharedPreferences("WebCheckerPrefs", Context.MODE_PRIVATE)
            val url = sharedPreferences.getString("url", null) ?: return Result.failure()
            val word = sharedPreferences.getString("word", null) ?: return Result.failure()

            // Conectar a la página web
            val doc = Jsoup.connect(url).get()

            // Verificar si la palabra está presente
            if (doc.text().contains(word, ignoreCase = true)) {
                sendNotification()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }

        return Result.success()
    }

    /**
     * Envía una notificación al usuario si se encuentra la palabra en la página web.
     *
     * <p>La notificación se muestra con el título "¡Se encontró la palabra!"
     * y un mensaje que indica que la palabra fue encontrada.</p>
     */
    private fun sendNotification() {
        val context = applicationContext

        // Crear el canal de notificación (solo necesario para API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "guestlist_channel",
                "Guestlist Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para notificaciones de palabras encontradas"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Crear y enviar la notificación
        val notification: Notification = NotificationCompat.Builder(context, "guestlist_channel")
            .setContentTitle("¡Se encontró la palabra!")
            .setContentText("La palabra fue encontrada en la página web.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }
}
