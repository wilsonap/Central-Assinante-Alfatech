package com.example.receipt

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ReceiptHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Cópia privada em filesDir/receipts + persistência Room.
 * Não carimba, não recomprime; cópia byte-a-byte.
 */
object ReceiptHistoryStore {

    private const val TAG = "RECEIPT_HISTORY"
    private const val DIR = "receipts"

    fun historyDir(context: Context): File {
        val dir = File(context.filesDir, DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun fileProviderUri(context: Context, absolutePath: String): Uri? {
        val file = File(absolutePath)
        if (!file.exists()) return null
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    suspend fun archiveAfterShareStarted(
        context: Context,
        sourceUri: Uri,
        mimeType: String,
        clientName: String,
        clientCode: String?,
        clientContract: String?
    ): Long? = withContext(Dispatchers.IO) {
        try {
            val mime = ReceiptShareHelper.resolveMime(context, sourceUri, mimeType)
            val ext = extensionFor(mime, sourceUri)
            val originalName = queryDisplayName(context, sourceUri)
            val dest = File(historyDir(context), "receipt_${System.currentTimeMillis()}.$ext")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            } ?: return@withContext null

            if (!dest.exists() || dest.length() <= 0L) {
                Log.w(TAG, "archive failed — empty copy")
                return@withContext null
            }

            val entity = ReceiptHistoryEntity(
                localFilePath = dest.absolutePath,
                mimeType = mime,
                originalFileName = originalName,
                clientName = clientName.trim().ifBlank { "Cliente" },
                clientCode = clientCode?.trim()?.takeIf { it.isNotBlank() },
                clientContract = clientContract?.trim()?.takeIf { it.isNotBlank() },
                createdAt = System.currentTimeMillis(),
                sentAt = null,
                status = ReceiptHistoryEntity.STATUS_SHARED
            )
            val id = AppDatabase.getDatabase(context).receiptHistoryDao().insert(entity)
            Log.i(TAG, "archived id=$id status=shared mime=$mime")
            id
        } catch (e: Exception) {
            Log.w(TAG, "archive fail=${e.javaClass.simpleName}")
            null
        }
    }

    suspend fun deleteEntry(context: Context, id: Long): Boolean = withContext(Dispatchers.IO) {
        val dao = AppDatabase.getDatabase(context).receiptHistoryDao()
        val entity = dao.getById(id) ?: return@withContext false
        runCatching {
            val file = File(entity.localFilePath)
            if (file.exists()) file.delete()
        }
        dao.deleteById(id)
        Log.i(TAG, "deleted id=$id")
        true
    }

    suspend fun storageStats(context: Context): Pair<Int, Long> = withContext(Dispatchers.IO) {
        val dao = AppDatabase.getDatabase(context).receiptHistoryDao()
        val count = dao.count()
        val bytes = historyDir(context).listFiles()?.sumOf { it.length() } ?: 0L
        count to bytes
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) cursor.getString(idx)?.takeIf { it.isNotBlank() } else null
                    } else null
                }
        } catch (_: Exception) {
            uri.lastPathSegment
        }
    }

    private fun extensionFor(mime: String, uri: Uri): String {
        if (mime.contains("pdf", ignoreCase = true)) return "pdf"
        MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)?.let { return it }
        val seg = uri.lastPathSegment.orEmpty()
        val dot = seg.lastIndexOf('.')
        if (dot >= 0 && dot < seg.length - 1) return seg.substring(dot + 1).take(8)
        return "jpg"
    }
}
