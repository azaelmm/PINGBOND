package com.example.pingbond

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pingbond.ui.theme.PINGBONDTheme


@Composable
fun LoginScreen() {
    var selectedTab by remember { mutableStateOf(0) } // 0 -> Login, 1 -> Register

    // Definir los estados de los campos
    val email = remember { mutableStateOf(TextFieldValue("")) }
    val password = remember { mutableStateOf(TextFieldValue("")) }
    val errorMessage = remember { mutableStateOf("") }

    // Caja que contiene todo el contenido de la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFBBDEFB)) // Fondo celeste
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Títulos y Tabs para seleccionar Login o Register
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Iniciar sesión") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Registrarse") }
                )
            }

            // Mostrar la vista de Login o Register dependiendo de la selección
            if (selectedTab == 0) {
                LoginForm(
                    email = email,
                    password = password,
                    errorMessage = errorMessage,
                    onLogin = {
                        // Aquí iría la lógica de login
                        if (email.value.text == "user@example.com" && password.value.text == "password123") {
                            errorMessage.value = ""
                            // Navegar a la pantalla principal
                        } else {
                            errorMessage.value = "Correo o contraseña incorrectos"
                        }
                    }
                )
            } else {
                RegisterForm(
                    email = email,
                    password = password,
                    onRegister = {
                        // Lógica para registrar usuario
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm(
    email: MutableState<TextFieldValue>,
    password: MutableState<TextFieldValue>,
    errorMessage: MutableState<String>,
    onLogin: () -> Unit
) {
    // Campo para ingresar el correo
    TextField(
        value = email.value,
        onValueChange = { email.value = it },
        label = { Text("Correo Electrónico") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.White
        )
    )

    // Campo para ingresar la contraseña
    TextField(
        value = password.value,
        onValueChange = { password.value = it },
        label = { Text("Contraseña") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.White
        )
    )

    // Mostrar mensaje de error si las credenciales no son correctas
    if (errorMessage.value.isNotEmpty()) {
        Text(
            text = errorMessage.value,
            color = Color.Red,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }

    // Botón de login
    Button(
        onClick = { onLogin() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = "Iniciar Sesión", fontSize = 16.sp)
    }

    // Enlace para registrarse
    Text(
        text = "¿No tienes una cuenta? Regístrate aquí.",
        color = Color(0xFF1E88E5),
        modifier = Modifier
            .clickable {
                // Cambiar al Tab de Registro
            }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterForm(
    email: MutableState<TextFieldValue>,
    password: MutableState<TextFieldValue>,
    onRegister: () -> Unit
) {
    // Campo para ingresar el correo
    TextField(
        value = email.value,
        onValueChange = { email.value = it },
        label = { Text("Correo Electrónico") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.White
        )
    )

    // Campo para ingresar la contraseña
    TextField(
        value = password.value,
        onValueChange = { password.value = it },
        label = { Text("Contraseña") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.White
        )
    )

    // Botón de registro
    Button(
        onClick = { onRegister() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = "Registrarse", fontSize = 16.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    PINGBONDTheme {
        LoginScreen()
    }
}
