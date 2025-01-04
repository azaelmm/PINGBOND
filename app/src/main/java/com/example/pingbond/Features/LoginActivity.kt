package com.example.pingbond.Features

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pingbond.R
import com.example.pingbond.ui.theme.OnPrimary
import com.example.pingbond.ui.theme.PINGBONDTheme
import com.example.pingbond.ui.theme.PrimaryVariant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa Firebase Auth
        auth = Firebase.auth

        setContent {
            PINGBONDTheme {
                val navController = rememberNavController()
                Navigation(navController = navController, auth = auth)
            }
        }
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth, navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) } // 0 -> Login, 1 -> Registro

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bglogin),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        ) // Aquí puedes añadir otros composables sobre la imagen de fondo

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(10.dp))
                    .background(color = PrimaryVariant)
                    .padding(vertical = 10.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {Text("Iniciar Sesión",
                        color =  Color.White,
                        fontSize = 16.sp
                    )}

                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {Text("Registrarse",
                        color = Color.White,
                        fontSize = 16.sp
                    )}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            if (selectedTab == 0) {
                LoginForm(onLogin = { email, password ->
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.i("LoginScreen", "Inicio de sesión exitoso")
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                Log.i(
                                    "LoginScreen",
                                    "Error al iniciar sesión: ${task.exception?.message}"
                                )
                            }
                        }
                })
            } else {
                RegisterForm(onRegister = { email, password ->
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.i("RegisterScreen", "Registro exitoso")
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                Log.i(
                                    "RegisterScreen",
                                    "Error al registrarse: ${task.exception?.message}"
                                )
                            }
                        }
                })
            }
        }
    }

}

@Composable
fun LoginForm(onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo Electrónico",
            icon = Icons.Default.Mail
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            icon = Icons.Default.Lock,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Por favor, completa todos los campos."
                } else {
                    errorMessage = ""
                    onLogin(email, password)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryVariant,
                contentColor = OnPrimary),

            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50)
        ) {
            Text(text = "Iniciar Sesión", fontSize = 16.sp)
        }
    }
}

@Composable
fun RegisterForm(onRegister: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }


    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        CustomTextField(
            value = username,
            onValueChange = { username = it },
            label = "Nombre de Usuario",
            icon = Icons.Default.Man
        )
        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo Electrónico",
            icon = Icons.Default.Mail
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            icon = Icons.Default.Lock,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    onRegister(email, password)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryVariant,
                contentColor = OnPrimary),

            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50)
        ) {
            Text(text = "Registrarse", fontSize = 16.sp)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent, shape = RoundedCornerShape(8.dp))
    )
}
