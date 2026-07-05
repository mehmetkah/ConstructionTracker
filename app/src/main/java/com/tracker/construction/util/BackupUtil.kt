package com.tracker.construction.util

import android.content.Context
import com.tracker.construction.data.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupUtil {

    private val fmt = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    private fun backupDir(context: Context): File {
        val dir = File(context.filesDir, "backups")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** Bundles the Room database file and every stored photo into a single zip backup. */
    fun createBackup(context: Context): File {
        val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
        val photosDir = File(context.filesDir, "photos")
        val outFile = File(backupDir(context), "SiteTracker_Backup_${fmt.format(Date())}.zip")

        ZipOutputStream(FileOutputStream(outFile)).use { zip ->
            if (dbFile.exists()) {
                zip.putNextEntry(ZipEntry("database/${AppDatabase.DB_NAME}"))
                FileInputStream(dbFile).use { it.copyTo(zip) }
                zip.closeEntry()
            }
            if (photosDir.exists()) {
                photosDir.listFiles()?.forEach { photo ->
                    zip.putNextEntry(ZipEntry("photos/${photo.name}"))
                    FileInputStream(photo).use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        }
        return outFile
    }

    /**
     * Restores a previously created backup zip. The app must restart afterward so Room
     * re-opens the restored database file cleanly.
     */
    fun restoreBackup(context: Context, backupFile: File) {
        val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
        val photosDir = File(context.filesDir, "photos")
        if (!photosDir.exists()) photosDir.mkdirs()

        // Close any open DB connection before overwriting the file.
        AppDatabase.getInstance(context).close()

        ZipInputStream(FileInputStream(backupFile)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val target = when {
                    entry.name.startsWith("database/") -> dbFile
                    entry.name.startsWith("photos/") -> File(photosDir, entry.name.removePrefix("photos/"))
                    else -> null
                }
                if (target != null) {
                    target.parentFile?.mkdirs()
                    FileOutputStream(target).use { out -> zip.copyTo(out) }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    fun listBackups(context: Context): List<File> =
        backupDir(context).listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
}
