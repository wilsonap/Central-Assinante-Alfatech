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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InvoiceEntity
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
    val statusLabel = visualStatus(invoice)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = statusLabel,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatBrl(invoice.amountCents),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Vencimento: ${formatDueDateBr(invoice.dueDate)}", fontSize = 13.sp)
            Text(text = "Cobrança: $billingLabel", fontSize = 13.sp)
            if (!invoice.rawBillingType.isNullOrBlank()) {
                Text(
                    text = "Tipo IXC: ${invoice.rawBillingType}",
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

private fun visualStatus(invoice: InvoiceEntity): String {
    val group = invoice.sourceGroup.orEmpty().lowercase(Locale.ROOT)
    when (group) {
        InvoiceEntity.GROUP_PAGAS -> return "Paga"
        InvoiceEntity.GROUP_CANCELADAS -> return "Cancelada"
        InvoiceEntity.GROUP_VENCIDAS -> return "Vencida"
        InvoiceEntity.GROUP_ABERTAS, InvoiceEntity.GROUP_PENDENTES -> return "Em aberto"
    }
    val text = invoice.statusText.orEmpty().lowercase(Locale.ROOT)
    return when {
        text.contains("cancel") -> "Cancelada"
        text.contains("paga") || text.contains("pago") -> "Paga"
        text.contains("vencid") -> "Vencida"
        else -> "Em aberto"
    }
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
