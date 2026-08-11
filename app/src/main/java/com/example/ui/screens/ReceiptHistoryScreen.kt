package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.ReceiptHistoryEntity
import com.example.receipt.ReceiptHistoryStore
import com.example.receipt.ReceiptShareHelper
import com.example.ui.WhatsAppSupport
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptHistoryScreen(
    receipts: List<ReceiptHistoryEntity>,
    supportWhatsAppNumber: String,
    storageCount: Int,
    storageBytes: Long,
    onClose: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf<ReceiptHistoryEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<ReceiptHistoryEntity?>(null) }

    val detail = selected
    if (detail != null) {
        ReceiptHistoryDetail(
            entity = detail,
            supportWhatsAppNumber = supportWhatsAppNumber,
            onBack = { selected = null },
            onDeleteRequest = { pendingDelete = detail },
            onResend = {
                val uri = ReceiptHistoryStore.fileProviderUri(context, detail.localFilePath)
                if (uri == null) {
                    Toast.makeText(context, "Arquivo local não encontrado.", Toast.LENGTH_SHORT).show()
                } else {
                    val message = ReceiptShareHelper.buildMessage(
                        fullName = detail.clientName,
                        clientCode = detail.clientCode.orEmpty(),
                        contract = detail.clientContract.orEmpty()
                    )
                    val started = ReceiptShareHelper.shareFileToWhatsApp(
                        context = context,
                        selectedUri = uri,
                        mimeType = detail.mimeType,
                        phoneDigits = supportWhatsAppNumber,
                        message = message
                    )
                    if (started) {
                        scope.launch {
                            ReceiptHistoryStore.markShared(context, detail.id)
                            selected = selected?.copy(status = ReceiptHistoryEntity.STATUS_SHARED)
                        }
                    }
                }
            },
            onOpenPdf = {
                val uri = ReceiptHistoryStore.fileProviderUri(context, detail.localFilePath)
                if (uri == null) {
                    Toast.makeText(context, "Arquivo local não encontrado.", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, detail.mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    runCatching { context.startActivity(Intent.createChooser(intent, "Abrir PDF")) }
                        .onFailure {
                            Toast.makeText(context, "Nenhum app para abrir PDF.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    title = {
                        Text("Comprovantes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                )
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Histórico local neste aparelho. $storageCount arquivo(s), ${formatBytes(storageBytes)}.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (receipts.isEmpty()) {
                    Text(
                        text = "Nenhum comprovante no histórico ainda.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(receipts, key = { it.id }) { item ->
                            ReceiptHistoryRow(entity = item, onClick = { selected = item })
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Excluir comprovante?") },
            text = {
                Text("O registro e a cópia local serão removidos. O arquivo original do aparelho não será apagado.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            ReceiptHistoryStore.deleteEntry(context, target.id)
                            pendingDelete = null
                            if (selected?.id == target.id) selected = null
                            onDeleted()
                        }
                    }
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ReceiptHistoryRow(
    entity: ReceiptHistoryEntity,
    onClick: () -> Unit
) {
    val isPdf = entity.mimeType.contains("pdf", ignoreCase = true)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isPdf) Color(0xFFFEE2E2) else Color(0xFFEBF2FE)),
                contentAlignment = Alignment.Center
            ) {
                if (isPdf) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null, modifier = Modifier.size(28.dp),
                        tint = Color(0xFFDC2626)
                    )
                } else {
                    val file = File(entity.localFilePath)
                    if (file.exists()) {
                        AndroidView(
                            factory = { ctx ->
                                ImageView(ctx).apply {
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                    setImageURI(Uri.fromFile(file))
                                }
                            }, modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entity.clientName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    formatDateTime(entity.createdAt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ReceiptHistoryStore.statusLabel(entity.status),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isPdf) "PDF" else "Imagem",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                entity.originalFileName?.takeIf { it.isNotBlank() }?.let { name ->
                    Text(name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptHistoryDetail(
    entity: ReceiptHistoryEntity,
    supportWhatsAppNumber: String,
    onBack: () -> Unit,
    onDeleteRequest: () -> Unit,
    onResend: () -> Unit,
    onOpenPdf: () -> Unit
) {
    val isPdf = entity.mimeType.contains("pdf", ignoreCase = true)
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                title = { Text("Detalhe", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                actions = {
                    IconButton(onClick = onDeleteRequest) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isPdf) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null, modifier = Modifier.size(40.dp),
                            tint = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = entity.originalFileName ?: "Comprovante PDF",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenPdf, modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Text("Abrir PDF")
                    }
                }
            } else {
                val file = File(entity.localFilePath)
                if (file.exists()) {
                    AndroidView(
                        factory = { ctx ->
                            ImageView(ctx).apply {
                                adjustViewBounds = true
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                setImageURI(Uri.fromFile(file))
                            }
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            Text("Dados do registro", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Cliente: ${entity.clientName}", fontSize = 13.sp)
            entity.clientCode?.let { Text("Código: $it", fontSize = 13.sp) }
            entity.clientContract?.let { Text("Contrato: $it", fontSize = 13.sp) }
            Text("Data: ${formatDateTime(entity.createdAt)}", fontSize = 13.sp)
            Text(
                "Status: ${ReceiptHistoryStore.statusLabel(entity.status)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "O Android não confirma se a mensagem foi enviada no WhatsApp.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Destino: WhatsApp da Alfatech (${WhatsAppSupport.maskNumber(ReceiptShareHelper.normalizePhone(supportWhatsAppNumber))})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onResend, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Text("Reenviar pelo WhatsApp", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onDeleteRequest, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Excluir")
            }
        }
    }
}

private fun formatDateTime(ms: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR")).format(Date(ms))

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
    return String.format(Locale.US, "%.1f MB", kb / 1024.0)
}
