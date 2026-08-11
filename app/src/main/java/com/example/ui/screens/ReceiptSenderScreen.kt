package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.receipt.ReceiptImageStamper
import com.example.receipt.ReceiptShareHelper
import com.example.ui.WhatsAppSupport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "RECEIPT_SEND"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptSenderScreen(
    clientFullName: String,
    supportWhatsAppNumber: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableStateOf(ReceiptStep.Pick) }
    var previewUri by remember { mutableStateOf<Uri?>(null) }
    var previewMime by remember { mutableStateOf("image/jpeg") }
    var isPdf by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    fun openPreview(uri: Uri, mime: String, pdf: Boolean) {
        if (pdf) {
            previewUri = uri
            previewMime = "application/pdf"
            isPdf = true
            step = ReceiptStep.Preview
            return
        }
        processing = true
        scope.launch {
            val stamped = withContext(Dispatchers.IO) {
                ReceiptImageStamper.stampCopy(context, uri, clientFullName)
            }
            processing = false
            if (stamped != null) {
                previewUri = stamped
                previewMime = "image/jpeg"
                isPdf = false
                step = ReceiptStep.Preview
            } else {
                Log.w(TAG, "preview prepare failed")
            }
        }
    }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        val uri = cameraUri
        if (ok && uri != null) openPreview(uri, "image/jpeg", pdf = false)
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) openPreview(uri, "image/*", pdf = false)
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
            openPreview(uri, "application/pdf", pdf = true)
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val pair = ReceiptImageStamper.createCameraTarget(context)
            cameraUri = pair.second
            takePicture.launch(pair.second)
        }
    }

    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            val pair = ReceiptImageStamper.createCameraTarget(context)
            cameraUri = pair.second
            takePicture.launch(pair.second)
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
                            previewUri = null
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
                processing = processing,
                onCamera = { launchCamera() },
                onGallery = { pickImage.launch("image/*") },
                onPdf = { pickPdf.launch(arrayOf("application/pdf")) }
            )

            ReceiptStep.Preview -> {
                val message = ReceiptShareHelper.buildMessage(clientFullName)
                PreviewPane(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    previewUri = previewUri,
                    isPdf = isPdf,
                    clientFullName = clientFullName,
                    supportMasked = WhatsAppSupport.maskNumber(
                        ReceiptShareHelper.normalizePhone(supportWhatsAppNumber)
                    ),
                    onChooseAnother = {
                        step = ReceiptStep.Pick
                        previewUri = null
                    },
                    onCancel = onClose,
                    onSendFile = {
                        val uri = previewUri ?: return@PreviewPane
                        ReceiptShareHelper.shareFileToWhatsApp(
                            context = context,
                            fileUri = uri,
                            mimeType = previewMime,
                            phoneDigits = supportWhatsAppNumber,
                            message = message
                        )
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
    processing: Boolean,
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
        if (processing) {
            Text("Preparando imagem…", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        }
        OptionCard(
            title = "Tirar foto",
            subtitle = "Usar a câmera do aparelho",
            icon = Icons.Default.CameraAlt,
            tint = Color(0xFF1A56DB),
            bg = Color(0xFFEBF2FE),
            onClick = onCamera,
            enabled = !processing
        )
        OptionCard(
            title = "Escolher imagem",
            subtitle = "Galeria ou arquivos de imagem",
            icon = Icons.Default.Image,
            tint = Color(0xFF059669),
            bg = Color(0xFFECFDF5),
            onClick = onGallery,
            enabled = !processing
        )
        OptionCard(
            title = "Escolher PDF",
            subtitle = "Arquivo PDF original, sem alteração",
            icon = Icons.Default.PictureAsPdf,
            tint = Color(0xFFDC2626),
            bg = Color(0xFFFEE2E2),
            onClick = onPdf,
            enabled = !processing
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
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
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
    previewUri: Uri?,
    isPdf: Boolean,
    clientFullName: String,
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
            text = "Confira o arquivo e os dados antes de enviar.",
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
                } else if (previewUri != null) {
                    var bitmap by remember(previewUri) { mutableStateOf<ImageBitmap?>(null) }
                    val ctx = LocalContext.current
                    LaunchedEffect(previewUri) {
                        bitmap = withContext(Dispatchers.IO) {
                            ctx.contentResolver.openInputStream(previewUri)?.use { input ->
                                BitmapFactory.decodeStream(input)?.asImageBitmap()
                            }
                        }
                    }
                    bitmap?.let { bmp ->
                        Image(
                            bitmap = bmp,
                            contentDescription = "Prévia do comprovante",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Dados incluídos", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Destino: WhatsApp da Alfatech ($supportMasked)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "Seu comprovante está pronto. Escolha como deseja enviar para a Alfatech.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = onSendFile,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
        ) {
            Text("Enviar comprovante", fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Escolha WhatsApp e depois selecione Alfatech Telecom.",
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
