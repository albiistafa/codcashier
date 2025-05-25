package dev.codcow.kasirku.features.auth.ui.screens

import android.content.Context
import androidx.compose.compiler.plugins.kotlin.inference.Token
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.theme.AppTheme
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(viewModel: LoginViewModel, navController: NavController, context: Context) {

    val uiState by viewModel.loginState.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val password by viewModel.password.collectAsState()
    val rememberMe by viewModel.rememberMe.collectAsState()
    val isFormValid = phoneNumber.isNotBlank() && password.isNotBlank()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSavedCredentials(context, viewModel)
    }

    fun onLoginSuccess(context: Context, phoneNumber: String, password: String) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("phone_number", phoneNumber)
            putString("password", password)
            apply()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is LoginState.Success) {
            onLoginSuccess(
                context, phoneNumber, password
            )
            navController.navigate("menu") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.primaryGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Masuk",
                style = AppTheme.typography.heading1Bold,
                color = AppTheme.colors.surface
            )
            Text(
                text = "Masukkan nomor telepon dan password untuk mulai menggunakan Kasirku",
                style = AppTheme.typography.paragraph1,
                color = AppTheme.colors.surface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 45.dp)
            )

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(21.dp)
                    .background(Color.White, shape = RoundedCornerShape(30.dp))
                    .padding(16.dp)
            ) {
                Text("No. Telepon",
                    style = AppTheme.typography.paragraph1,
                    textAlign = TextAlign.Start)
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { viewModel.onPhoneNumberChange(it) },
                    placeholder = { Text("Masukkan nomor telepon", style = AppTheme.typography.paragraph2) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(30.dp),
                    textStyle = AppTheme.typography.paragraph2,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.colors.surface, // Ganti dengan warna yang diinginkan
                        unfocusedBorderColor = Color.Gray // Ganti dengan warna untuk border saat tidak fokus
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Password", style = AppTheme.typography.paragraph1)
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    placeholder = { Text("Masukkan password", style = AppTheme.typography.paragraph2) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = AppTheme.colors.surface// Warna sesuai tema
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(30.dp),
                    textStyle = AppTheme.typography.paragraph2,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.colors.surface,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { viewModel.onRememberMeChange(it) },
                        modifier = Modifier.scale(0.75f),
                        colors = CheckboxDefaults.colors(
                            checkedColor = AppTheme.colors.surface, // Warna ketika Checkbox terpilih
                            uncheckedColor = Color.Gray, // Warna ketika Checkbox tidak terpilih
                            checkmarkColor = Color.White // Warna checkmark
                        )
                    )
                    Text("Ingat saya", style = AppTheme.typography.paragraph2)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.onLoginClick() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormValid) AppTheme.colors.surface else Color.Gray
                    ),
                    enabled = isFormValid
                ) {
                    if (uiState is LoginState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Selanjutnya", color = Color.White, style = AppTheme.typography.paragraph1)
                    }
                }

                if (uiState is LoginState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (uiState as LoginState.Error).message,
                        color = Color.Red,
                        style = AppTheme.typography.paragraph2
                    )
                }
            }
        }
    }
}



