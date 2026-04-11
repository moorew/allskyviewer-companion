package de.astronarren.allsky.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import java.io.File

import de.astronarren.allsky.data.UserPreferences

class DownloadHelper(
    private val context: Context,
    private val userPreferences: UserPreferences
) {
    suspend fun downloadMedia(url: String, fileName: String, isVideo: Boolean = false) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription("Downloading Allsky media...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES,
                    "Allsky/$fileName"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val username = userPreferences.getUsername()
            val password = userPreferences.getPassword()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                val auth = "Basic " + android.util.Base64.encodeToString(
                    "$username:$password".toByteArray(),
                    android.util.Base64.NO_WRAP
                )
                request.addRequestHeader("Authorization", auth)
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            
            Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
