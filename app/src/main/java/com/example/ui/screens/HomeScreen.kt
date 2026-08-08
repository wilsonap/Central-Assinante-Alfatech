package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CentralShortcut(
    val title: String,
    val subtitle: String,
    val urlPath: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color
)

@Composable
fun HomeScreen(
    onNavigateToUrl: (urlPath: String, title: String) -> Unit
) {
    val shortcuts = listOf(
        CentralShortcut(
            title = "Faturas",
            subtitle = "2ª via, código de barras e Pix",
            urlPath = "faturas",
            icon = Icons.Default.Receipt,
            iconTint = Color(0xFF1A56DB),
            iconBackground = Color(0xFFEBF2FE)
        ),
        CentralShortcut(
            title = "Contratos / Planos",
            subtitle = "Planos e termos do contrato",
            urlPath = "planos",
            icon = Icons.Default.Description,
            iconTint = Color(0xFF059669),
            iconBackground = Color(0xFFECFDF5)
        ),
        CentralShortcut(
            title = "Consumo",
            subtitle = "Histórico de utilização de banda",
            urlPath = "consumos",
            icon = Icons.Default.BarChart,
            iconTint = Color(0xFFD97706),
            iconBackground = Color(0xFFFEF3C7)
        ),
        CentralShortcut(
            title = "Suporte / Atendimento",
            subtitle = "Abrir e acompanhar chamados técnicos",
            urlPath = "atendimentos",
            icon = Icons.Default.HeadsetMic,
            iconTint = Color(0xFF7C3AED),
            iconBackground = Color(0xFFF3E8FF)
        ),
        CentralShortcut(
            title = "Meus Dados",
            subtitle = "Informações do cadastro do assinante",
            urlPath = "dados_cliente",
            icon = Icons.Default.Person,
            iconTint = Color(0xFFDB2777),
            iconBackground = Color(0xFFFCE7F3)
        ),
        CentralShortcut(
            title = "Relatórios",
            subtitle = "Extratos e histórico da conta",
            urlPath = "relatorios",
            icon = Icons.Default.Assignment,
            iconTint = Color(0xFF0891B2),
            iconBackground = Color(0xFFCFFAFE)
        ),
        CentralShortcut(
            title = "Pagamento Recorrente",
            subtitle = "Configurações de cartão e cobrança",
            urlPath = "configuracoes",
            icon = Icons.Default.CreditCard,
            iconTint = Color(0xFF4F46E5),
            iconBackground = Color(0xFFE0E7FF)
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Compact Professional Header (Alfatech Telecom - Central do Assinante)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "A",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Alfatech Telecom",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Central do Assinante",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Compact button to open complete Central
                        Button(
                            onClick = { onNavigateToUrl("", "Central do Assinante") },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(
                                text = "Abrir Central Completa",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Acesse rapidamente seus principais serviços.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "SERVIÇOS E ATALHOS RÁPIDOS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // List of Shortcuts
        items(shortcuts.size) { index ->
            val shortcut = shortcuts[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigateToUrl(shortcut.urlPath, shortcut.title)
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
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
                            .background(shortcut.iconBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = shortcut.icon,
                            contentDescription = shortcut.title,
                            tint = shortcut.iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = shortcut.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = shortcut.subtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
