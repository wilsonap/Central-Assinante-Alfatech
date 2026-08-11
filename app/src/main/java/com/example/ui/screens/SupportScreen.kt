package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SupportScreen() {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Canais de Atendimento Alfatech",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Estamos prontos para ajudar você com sua conexão de fibra óptica.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            SupportContactCard(
                title = "Atendimento via WhatsApp",
                subtitle = "Fale diretamente com nossa equipe de suporte técnico",
                icon = Icons.AutoMirrored.Filled.Chat,
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/5508000000000"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sac2.alfatechtelecom.com.br/central_assinante_web/"))
                        context.startActivity(intent)
                    }
                }
            )
        }

        item {
            SupportContactCard(
                title = "Central de Atendimento Telefônico",
                subtitle = "Ligue gratuitamente para o SAC Alfatech",
                icon = Icons.Default.Call,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:08000000000"))
                    context.startActivity(intent)
                }
            )
        }

        item {
            SupportContactCard(
                title = "Portal Web Central do Assinante",
                subtitle = "Acesse sac2.alfatechtelecom.com.br no navegador",
                icon = Icons.Default.Language,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sac2.alfatechtelecom.com.br/central_assinante_web/"))
                    context.startActivity(intent)
                }
            )
        }

        item {
            Text(
                text = "Diagnóstico e Dicas de Conexão",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            SupportContactCard(
                title = "Teste sua Velocidade (Speedtest)",
                subtitle = "Verifique a taxa de download e upload da sua fibra",
                icon = Icons.Default.Speed,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.speedtest.net"))
                    context.startActivity(intent)
                }
            )
        }

        item {
            SupportContactCard(
                title = "Reiniciar Roteador Wi-Fi",
                subtitle = "Passos simples: Desligue da tomada por 30 segundos e ligue novamente.",
                icon = Icons.Default.Router,
                onClick = {}
            )
        }
    }
}

@Composable
private fun SupportContactCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
