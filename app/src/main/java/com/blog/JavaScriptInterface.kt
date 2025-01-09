package com.blog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream

class JavaScriptInterface(private val context: Context) {

    private val CHANNEL_ID = "download_channel"

    init {
        createNotificationChannel()
    }
    @JavascriptInterface
    fun downloadBlob(base64Data: String, fileName: String) {
        try {


            // Base64 데이터 디코딩
            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)

            var fileUri: Uri? = null
            var filePath: String? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 이상: MediaStore에 저장
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                }

                val resolver = context.contentResolver
                fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                if (fileUri != null) {
                    resolver.openOutputStream(fileUri)?.use { outputStream ->
                        outputStream.write(decodedBytes)
                        outputStream.flush()
                        filePath = "Downloads/$fileName"
                    }
                } else {
                    Toast.makeText(context, "Failed to save file: $fileName", Toast.LENGTH_SHORT).show()
                    return
                }
            } else {
                // Android 9 이하: 외부 저장소에 저장
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(decodedBytes)
                    outputStream.flush()
                }
                fileUri = Uri.fromFile(file)
                filePath = file.absolutePath
            }

            // 알림 표시
            if (fileUri != null) {
                showNotification(fileName, fileUri)
                Toast.makeText(context, "Downloaded to: $filePath", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // 오류 처리
            Toast.makeText(context, "Error downloading file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Download Notifications"
            val descriptionText = "Notifications for file downloads"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun showNotification(fileName: String, fileUri: Uri) {
        try {
            // Intent 생성
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, getMimeType(fileUri, fileName))
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            // PendingIntent 생성
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 알림 생성
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Download Complete")
                .setContentText("Downloaded: $fileName")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            // 알림 표시
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            Log.e("NotificationError", "Error creating notification: ${e.message}")
        }
    }

    private fun getMimeType(fileUri: Uri, fileName: String): String {
        return try {
            // 1. ContentResolver를 통해 MIME 타입 확인
            val contentResolver = context.contentResolver
            val mimeTypeFromUri = contentResolver.getType(fileUri)
            if (mimeTypeFromUri != null) {
                return mimeTypeFromUri
            }

            // 2. 파일 확장자를 통해 MIME 타입 확인
            val extension = fileName.substringAfterLast('.', "").lowercase()
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
        } catch (e: Exception) {
            Log.e("MimeTypeError", "Error detecting MIME type: ${e.message}")
            "application/octet-stream" // 기본 MIME 타입
        }
    }

}