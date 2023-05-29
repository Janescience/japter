package japter.tool

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class NotificationExfiltrator : NotificationProcessor {
    override fun getName(): String {
        return "eft"
    }

    override fun process(notificationData: NotificationData, context: Context): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val publicKey = sharedPreferences.getString("publicKey", null)
            ?: return "[Public key not set up]"

        if (publicKey.length != 44) {
            return "[Invalid public key length]"
        }

        val data = notificationData
            .let { Json.encodeToString(it) }
            .let { Sealer().seal(it, publicKey) }
        Log.d("SealerData", data)

        val request = OneTimeWorkRequestBuilder<ExfiltrateWork>()
            .setInputData(workDataOf("data" to data))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("eft")
            .build()
        WorkManager.getInstance(context).enqueue(request)
        return request.id.toString()
    }
}

class ExfiltrateWork(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val data =
            inputData.getString("data") ?: return Result.failure()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val endpoint =
            sharedPreferences.getString("endpoint", null) ?: return Result.failure()
        try {
            val url = URL(endpoint)
            Log.d("JapterAPIConnect", "Connecting to $endpoint")
            val connection = url.openConnection() as HttpURLConnection

            try {
                val bytes = data.encodeToByteArray()
                Log.d("JapterAPIConnect", "Data encoded to bytes is  ${bytes.size}")
                connection.doOutput = true
                connection.setFixedLengthStreamingMode(bytes.size)
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "text/plain")
                connection.outputStream.write(bytes)
                val responseCode = connection.responseCode
                Log.d("JapterAPIConnect", "Response Code : $responseCode")
                if (responseCode != 200) {
                    Log.e("JapterAPIConnect", "Retry next time because unsuccessfully.")
                    return Result.retry()
                }
            } catch (error: IOException) {
                Log.e("JapterAPIConnect", "IO exception : ", error)
                return Result.retry()
            } finally {
                connection.disconnect()
            }
        } catch (error: Exception) {
            Log.e("JapterAPIConnect", "Can't connected : ", error)
            return Result.failure()
        }

        return Result.success()
    }
}
