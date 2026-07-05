package com.tracker.construction.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageUtil {

    private fun photosDir(context: Context): File {
        val dir = File(context.filesDir, "photos")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** Creates an empty destination file + content:// Uri for the camera to write a new photo into. */
    fun createCameraDestination(context: Context): Pair<File, Uri> {
        val file = File(photosDir(context), "IMG_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "com.tracker.construction.fileprovider", file)
        return file to uri
    }

    /** Copies a gallery-picked image into the app's private storage so it survives offline. */
    fun copyFromUri(context: Context, source: Uri): String? {
        return try {
            val dest = File(photosDir(context), "IMG_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(source)?.use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }
            dest.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
