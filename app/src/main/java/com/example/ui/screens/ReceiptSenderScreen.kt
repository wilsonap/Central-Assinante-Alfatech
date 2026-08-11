package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.receipt.ReceiptHistoryStore
import com.example.receipt.ReceiptShareHelper
import com.example.ui.WhatsAppSupport
import kotlinx.coroutines.launch

private const val URI_LOG = "RECEIPT_SHARE"

/**
 * Fluxo: selectedUri → preview → EXTRA_STREAM (mesma URI).
 * Após iniciar o share, arquiva cópia local no histórico.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptSenderScreen(
    clientFullName: String,
    clientCode: String,
    clientContract: String,
    supportWhatsAppNumber: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableStateOf(ReceiptStep.Pick) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedMime by remember { mutableStateOf("image/*") }
    var isPdf by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    fun bindSelected(uri: Uri, mime: String, pdf: Boolean) {
        selectedUri = uri
        isPdf = pdf
        selectedMime = if (pdf) {
            "application/pdf"
        } else {
            ReceiptShareHelper.resolveMime(context, uri, mime)
        }
        val originalId = ReceiptShareHelper.uriTraceId(uri)
        val previewId = ReceiptShareHelper.uriTraceId(uri)
        Log.i(URI_LOG, "RECEIPT_ORIGINAL_URI id=$originalId")
        Log.i(URI_LOG, "RECEIPT_PREVIEW_URI id=$previewId")
        Log.i(URI_LOG, "sameOriginalPreview=${originalId == previewId}")
        step = ReceiptStep.Preview
    }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        val uri = cameraUri
        if (ok && uri != null) bindSelected(uri, "image/jpeg", pdf = false)
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) bindSelected(uri, "image/*", pdf = false)
    }

    val pickPdf = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            bindSelected(uri, "application/pdf", pdf = true)
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = ReceiptShareHelper.createCameraCaptureUri(context)
            cameraUri = uri
            takePicture.launch(uri)
        }
    }

    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            val uri = ReceiptShareHelper.createCameraCaptureUri(context)
            cameraUri = uri
            takePicture.launch(uri)
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        if (step == ReceiptStep.Preview) {
                            step = ReceiptStep.Pick
                            selectedUri = null
                        } else {
                            onClose()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                title = {
                    Text(
                        text = if (step == ReceiptStep.Pick) "Enviar comprovante" else "Confirmar envio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            )
        }
    ) { padding ->
        when (step) {
            ReceiptStep.Pick -> PickOptions(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                onCamera = { launchCamera() },
                onGallery = { pickImage.launch("image/*") },
                onPdf = { pickPdf.launch(arrayOf("application/pdf")) }
            )

            ReceiptStep.Preview -> {
                val message = ReceiptShareHelper.buildMessage(
                    fullName = clientFullName,
                    clientCode = clientCode,
                    contract = clientContract
                )
                val uri = selectedUri
                PreviewPane(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    selectedUri = uri,
                    isPdf = isPdf,
                    clientFullName = clientFullName,
                    clientCode = clientCode,
                    clientContract = clientContract,
                    supportMasked = WhatsAppSupport.maskNumber(
                        ReceiptShareHelper.normalizePhone(supportWhatsAppNumber)
                    ),
                    onChooseAnother = {
                        step = ReceiptStep.Pick
                        selectedUri = null
                    },
                    onCancel = onClose,
                    onSendFile = {
                        if (uri == null) return@PreviewPane
                        val originalId = ReceiptShareHelper.uriTraceId(uri)
                        val shareId = ReceiptShareHelper.uriTraceId(uri)
                        Log.i(URI_LOG, "RECEIPT_ORIGINAL_URI id=$originalId")
                        Log.i(URI_LOG, "RECEIPT_SHARE_URI id=$shareId")
                        Log.i(URI_LOG, "sameOriginalShare=${originalId == shareId}")
                        scope.launch {
                            val historyId = ReceiptHistoryStore.archivePrepared(
                                context = context,
                                sourceUri = uri,
                                mimeType = selectedMime,
                                clientName = clientFullName,
                                clientCode = clientCode,
                                clientContract = clientContract
                            )
                            val started = ReceiptShareHelper.shareFileToWhatsApp(
                                context = context,
                                selectedUri = uri,
                                mimeType = selectedMime,
                                phoneDigits = supportWhatsAppNumber,
                                message = message
                            )
                            if (started && historyId != null) {
                                ReceiptHistoryStore.markShared(context, historyId)
                            }
                        }
                    },
                    onOpenChat = {
                        ReceiptShareHelper.openAlfatechChat(
                            context = context,
                            phoneDigits = supportWhatsAppNumber,
                            message = message
                        )
                    }
                )
            }
        }
    }
}

private enum class ReceiptStep { Pick, Preview }

@Composable
private fun PickOptions(
    modifier: Modifier,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onPdf: () -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Escolha como deseja enviar o comprovante",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OptionCard(
            title = "Tirar foto",
            subtitle = "Usar a câmera do aparelho",
            icon = Icons.Default.CameraAlt,
            tint = Color(0xFF1A56DB),
            bg = Color(0xFFEBF2FE),
            onClick = onCamera
        )
        OptionCard(
            title = "Escolher imagem",
            subtitle = "Galeria ou arquivos de imagem",
            icon = Icons.Default.Image,
            tint = Color(0xFF059669),
            bg = Color(0xFFECFDF5),
            onClick = onGallery
        )
        OptionCard(
            title = "Escolher PDF",
            subtitle = "Arquivo PDF original, sem alteração",
            icon = Icons.Default.PictureAsPdf,
            tint = Color(0xFFDC2626),
            bg = Color(0xFFFEE2E2),
            onClick = onPdf
        )
    }
}

@Composable
private fun OptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    bg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PreviewPane(
    modifier: Modifier,
    selectedUri: Uri?,
    isPdf: Boolean,
    clientFullName: String,
    clientCode: String,
    clientContract: String,
    supportMasked: String,
    onChooseAnother: () -> Unit,
    onCancel: () -> Unit,
    onSendFile: () -> Unit,
    onOpenChat: () -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Confira o arquivo original e os dados da mensagem antes de enviar.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isPdf) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("PDF selecionado (original, sem alteração)", fontWeight = FontWeight.SemiBold)
                    }
                } else if (selectedUri != null) {
                    // Prévia visual via ImageView.setImageURI — sem Canvas/stamp/arquivo novo.
                    AndroidView(
                        factory = { ctx ->
                            ImageView(ctx).apply {
                                adjustViewBounds = true
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                setImageURI(selectedUri)
                            }
                        },
                        update = { view -> view.setImageURI(selectedUri) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Dados na mensagem do WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = "Esses dados vão no texto da mensagem — não são impressos no comprovante.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (clientFullName.isNotBlank()) {
                    Text("Cliente: $clientFullName", fontSize = 13.sp)
                } else {
                    Text(
                        "Nome completo ainda não disponível. Abra a Central para carregar Meus Dados.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (clientCode.isNotBlank()) {
                    Text("Código: $clientCode", fontSize = 13.sp)
                }
                if (clientContract.isNotBlank()) {
                    Text("Contrato: $clientContract", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Destino: WhatsApp da Alfatech ($supportMasked)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = onSendFile,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
        ) {
            Text("Enviar pelo WhatsApp", fontWeight = FontWeight.Bold)
        }
        Text(
            text = "O arquivo original e os dados do comprovante serão compartilhados.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = onOpenChat,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Abrir conversa da Alfatech")
        }
        Text(
            text = "Abre diretamente o atendimento da Alfatech. O comprovante deverá ser anexado manualmente.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = onChooseAnother,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Escolher outro arquivo")
        }
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cancelar")
        }
    }
}
