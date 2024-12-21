package com.example.pingbond

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.pingbond.ui.theme.PINGBONDTheme


class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PINGBONDTheme {
                DashboardScreen(onNavigate = { /* Handle navigation */ })
            }
        }
    }
}

@Composable
fun DashboardScreen(onNavigate: (String) -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Header
            HeaderSection()

            // Main options
            MainOptionsSection { route -> onNavigate(route) }

            // Footer
            FooterSection()
        }
    }
}

@Composable
fun HeaderSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenido, Usuario!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
        )
        Text(
            text = "Conecta, chatea y comparte momentos",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MainOptionsSection(onOptionClick: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val options = listOf(
            DashboardOption("Mensajes", Icons.Default.MailOutline, Color(0xFF42A5F5), "messages"),
            DashboardOption("Contactos", Icons.Default.Person, Color(0xFF29B6F6), "contacts"),
            DashboardOption("Llamadas", Icons.Default.Call, Color(0xFF26C6DA), "calls"),
            DashboardOption("Notificaciones", Icons.Default.Notifications, Color(0xFF00ACC1), "notifications")
        )

        options.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowOptions.forEach { option ->
                    OptionCard(
                        title = option.title,
                        icon = option.icon,
                        backgroundColor = option.color,
                        onClick = { onOptionClick(option.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun FooterSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Text(
            text = "PingBond © 2024",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
fun OptionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color = backgroundColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

data class DashboardOption(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val route: String
)

@Preview(showBackground = true)
@Composable
fun PreviewDashboardScreen() {
    PINGBONDTheme {
        DashboardScreen(onNavigate = {})
    }
}
