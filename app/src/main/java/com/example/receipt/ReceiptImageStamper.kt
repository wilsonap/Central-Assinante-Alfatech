package com.example.receipt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Cria cópia em cache da imagem com rodapé discreto. Nunca altera o arquivo original.
 * Rodapé: Alfatech Telecom + Cliente (nome completo) + Enviado em.
 */
object ReceiptImageStamper {

    private const val TAG = "RECEIPT_SEND"
    private const val DIR = "receipts"

    fun prepareCacheDir(context: Context): File {
        val dir = File(context.cacheDir, DIR)
        if (!dir.exists()) dir.mkdirs()
        cleanupOld(dir)
        return dir
    }

    fun cleanupOld(dir: File, maxAgeMs: Long = 24L * 60 * 60 * 1000) {
        val now = System.currentTimeMillis()
        dir.listFiles()?.forEach { f ->
            if (now - f.lastModified() > maxAgeMs) {
                runCatching { f.delete() }
            }
        }
    }

    fun createCameraTarget(context: Context): Pair<File, Uri> {
        val dir = prepareCacheDir(context)
        val file = File(dir, "camera_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return file to uri
    }

    fun stampCopy(
        context: Context,
        sourceUri: Uri,
        fullName: String
    ): Uri? {
        return try {
            val original = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return null

            val footerLines = buildFooterLines(fullName)
            val width = original.width.coerceAtLeast(100)

            // Dynamic scale based on original image width to keep stamp perfectly crisp and proportional
            val textSizePx = (width * 0.032f).coerceIn(24f, 120f)
            val paddingPx = (width * 0.03f).coerceIn(20f, 100f)
            val lineHeightPx = textSizePx * 1.35f
            val footerHeight = (paddingPx * 2 + lineHeightPx * footerLines.size).toInt()

            val height = original.height + footerHeight
            val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(original, 0f, 0f, null)

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
            canvas.drawRect(
                0f,
                original.height.toFloat(),
                width.toFloat(),
                height.toFloat(),
                bgPaint
            )

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F172A")
                textSize = textSizePx
                isFakeBoldText = true
            }
            var y = original.height + paddingPx + textSizePx
            footerLines.forEach { line ->
                canvas.drawText(line, paddingPx, y, textPaint)
                y += lineHeightPx
            }

            val dir = prepareCacheDir(context)
            val outFile = File(dir, "stamped_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { fos ->
                out.compress(Bitmap.CompressFormat.JPEG, 98, fos)
            }
            if (!original.isRecycled) original.recycle()
            if (!out.isRecycled) out.recycle()

            Log.i(TAG, "image stamped copy ready")
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
        } catch (e: Exception) {
            Log.w(TAG, "stamp failed: ${e.javaClass.simpleName}")
            null
        }
    }

    private fun buildFooterLines(fullName: String): List<String> {
        val whenStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR")).format(Date())
        return buildList {
            add("Alfatech Telecom")
            if (fullName.isNotBlank()) add("Cliente: ${fullName.trim()}")
            add("Enviado em: $whenStr")
        }
    }
}
