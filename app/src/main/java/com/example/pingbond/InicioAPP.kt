package com.example.pingbond

import android.content.Intent
import android.icu.text.ListFormatter.Width
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pingbond.R
import com.example.pingbond.ui.theme.PINGBONDTheme
import kotlinx.coroutines.delay
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {
    // Agregamos LaunchedEffect para manejar el retraso
    LaunchedEffect(Unit) {
        delay(5000) // Espera 10 segundos
        onNavigateToLogin() // Llama a la función de navegación proporcionada
    }

    // Fondo con gradiente
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E88E5), // Azul
                        Color(0xFF64B5F6), // Azul Claro
                        Color(0xFFBBDEFB)  // Celeste
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logoimagen_pingbond),
                contentDescription = "PingBond Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // App Name
            Text(
                text = "PingBond",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = "Conéctate al mundo en segundos...",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            // Loader (debajo del texto)
            Spacer(modifier = Modifier.height(20.dp))  // Espaciado entre el texto y el loader
            AtomicLoader(modifier = Modifier.size(60.dp))  // El tamaño del loader
        }
    }
}

/**
 * A composable function that draws a rotating circle with a border effect.
 *
 * @param modifier Modifier to be applied to the circle's drawing area.
 * @param rotationX Rotation angle around the X-axis in degrees.
 * @param rotationY Rotation angle around the Y-axis in degrees.
 * @param rotationZ Rotation angle around the Z-axis in degrees.
 * @param borderColor Color of the circle's border.
 * @param borderWidth Width of the circle's border.
 */
@Composable
fun RotatingCircle(
    modifier: Modifier,
    rotationX: Float,
    rotationY: Float,
    rotationZ: Float,
    borderColor: Color,
    borderWidth: Dp
) {
    Canvas(
        modifier = modifier.graphicsLayer {
            this.rotationX = rotationX
            this.rotationY = rotationY
            this.rotationZ = rotationZ
            cameraDistance = 12f * density
        }
    ) {
        val mainCircle = Path().apply {
            addOval(Rect(size.center, size.minDimension / 2))
        }

        val clipCenter = Offset(size.width / 2f - borderWidth.toPx(), size.height / 2f)
        val clipCircle = Path().apply {
            addOval(Rect(clipCenter, size.minDimension / 2))
        }

        val path = Path().apply {
            op(mainCircle, clipCircle, PathOperation.Difference)
        }

        drawPath(path, borderColor)
    }
}

@Composable
fun AtomicLoader(
    modifier: Modifier,
    color: Color = Color.Blue,
    borderWidth: Dp = 3.dp,
    cycleDuration: Int = 1000
) {
    val infiniteTransition = rememberInfiniteTransition("InfiniteAtomicLoaderTransition")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(cycleDuration, easing = LinearEasing)
        ),
        label = "AtomicLoaderRotation"
    )

    Box(modifier) {
        RotatingCircle(
            modifier = Modifier.matchParentSize(),
            rotationX = 35f,
            rotationY = -45f,
            rotationZ = -90f + rotation,
            borderColor = color,
            borderWidth = borderWidth
        )
        RotatingCircle(
            modifier = Modifier.matchParentSize(),
            rotationX = 50f,
            rotationY = 10f,
            rotationZ = rotation,
            borderColor = color,
            borderWidth = borderWidth
        )
        RotatingCircle(
            modifier = Modifier.matchParentSize(),
            rotationX = 35f,
            rotationY = 55f,
            rotationZ = 90f + rotation,
            borderColor = color,
            borderWidth = borderWidth
        )
    }
}