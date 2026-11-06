package com.docsalert.reminder.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object VaultFileHelper {
    private const val TAG = "VaultFileHelper"
    private const val ATTACHMENT_DIR = "vault_attachments"

    private fun getAttachmentsDirectory(context: Context): File {
        val dir = File(context.filesDir, ATTACHMENT_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun saveUriToPrivateStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(sourceUri)
            val extension = when {
                mimeType?.contains("pdf") == true -> "pdf"
                mimeType?.contains("png") == true -> "png"
                mimeType?.contains("jpeg") == true || mimeType?.contains("jpg") == true -> "jpg"
                else -> {
                    val path = sourceUri.path ?: ""
                    if (path.endsWith(".pdf", ignoreCase = true)) "pdf" else "jpg"
                }
            }

            val fileName = "vault_${UUID.randomUUID()}.$extension"
            val targetFile = File(getAttachmentsDirectory(context), fileName)

            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            targetFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save attachment to private storage", e)
            null
        }
    }

    fun openPdfFile(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "PDF file does not exist at path: $filePath")
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening PDF file: $filePath", e)
        }
    }

    fun deleteFileAtPath(filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete file at $filePath", e)
        }
    }
}
