package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InvoiceEntity
import com.example.invoice.InvoiceDisplayStatus
import com.example.invoice.InvoiceDisplayStatusMapper
import com.example.invoice.InvoiceVisualKind
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoicesScreen(
    invoices: List<InvoiceEntity>,
    lastSyncedAt: Long?,
    remindDayBefore: Boolean,
    remindDueDate: Boolean,
    onRemindDayBeforeChange: (Boolean) -> Unit,
    onRemindDueDateChange: (Boolean) -> Unit,
    onOpenCentralInvoices: () -> Unit
) {
    val context = LocalContext.current
    val lastSyncLabel = formatLastSync(lastSyncedAt)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Faturas",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "Dados salvos no aparelho",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "Última atualização: $lastSyncLabel",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOpenCentralInvoices,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abrir na Central do Assinante", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Lembretes de faturas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ReminderToggleRow(
                        label = "Avisar 1 dia antes",
                        checked = remindDayBefore,
                        onCheckedChange = onRemindDayBeforeChange
                    )
                    ReminderToggleRow(
                        label = "Avisar no dia do vencimento",
                        checked = remindDueDate,
                        onCheckedChange = onRemindDueDateChange
                    )
                }
            }
        }

        if (invoices.isEmpty()) {
            item {
                Text(
                    text = "Nenhuma fatura sincronizada ainda. Abra a Central para atualizar.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            items(invoices, key = { it.idReceber }) { invoice ->
                InvoiceCard(
                    invoice = invoice,
                    onCopyBarcode = { barcode ->
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("codigo_barras", barcode))
                        Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun ReminderToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun InvoiceCard(
    invoice: InvoiceEntity,
    onCopyBarcode: (String) -> Unit
) {
    val display = InvoiceDisplayStatusMapper.getInvoiceDisplayStatus(invoice)
    val billingLabel = when (invoice.billingType) {
        InvoiceEntity.BILLING_BANK -> "Bancária"
        InvoiceEntity.BILLING_STORE -> "Loja / presencial"
        else -> "Consultar na Central"
    }
    val barcode = invoice.barcode?.takeIf { it.isNotBlank() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Valor (principal) + status chip — valor nunca perde espaço em tela pequena
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = formatBrl(invoice.amountCents),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                InvoiceStatusChip(display = display)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Vencimento",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatDueDateBr(invoice.dueDate),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cobrança: $billingLabel",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!invoice.rawBillingType.isNullOrBlank()) {
                Text(
                    text = "Tipo: ${invoice.rawBillingType}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (barcode != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onCopyBarcode(barcode) },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar código de barras", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun InvoiceStatusChip(display: InvoiceDisplayStatus) {
    val (bg, fg) = statusChipColors(display.kind)
    Text(
        text = display.label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = fg,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .widthIn(max = 148.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun statusChipColors(kind: InvoiceVisualKind): Pair<Color, Color> = when (kind) {
    InvoiceVisualKind.PAID -> Color(0xFFD1FAE5) to Color(0xFF047857)
    InvoiceVisualKind.OPEN_FUTURE -> Color(0xFFEBF2FE) to MaterialTheme.colorScheme.primary
    InvoiceVisualKind.DUE_TOMORROW -> Color(0xFFFEF3C7) to Color(0xFFB45309)
    InvoiceVisualKind.DUE_TODAY -> Color(0xFFFFEDD5) to Color(0xFFC2410C)
    InvoiceVisualKind.OVERDUE -> Color(0xFFFEE2E2) to MaterialTheme.colorScheme.error
    InvoiceVisualKind.CANCELLED ->
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    InvoiceVisualKind.FALLBACK ->
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
}

private fun formatBrl(cents: Long): String {
    val reais = cents / 100
    val frac = (cents % 100).toInt().toString().padStart(2, '0')
    return "R$ $reais,$frac"
}

private fun formatDueDateBr(iso: String): String {
    val parts = iso.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
}

private fun formatLastSync(epochMs: Long?): String {
    if (epochMs == null || epochMs <= 0L) return "ainda não sincronizado"
    val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))
    return fmt.format(Date(epochMs))
}
