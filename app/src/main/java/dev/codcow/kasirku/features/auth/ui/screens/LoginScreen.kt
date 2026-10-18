package dev.codcow.kasirku.features.auth.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
            .background(AppTheme.colors.primaryGradient)
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Masuk",
                style = AppTheme.typography.heading1Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Masukkan nomor telepon dan password untuk mulai menggunakan Kasirku",
                style = AppTheme.typography.paragraph1,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 45.dp)
            )

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(21.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(30.dp))
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
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(30.dp),
                    textStyle = AppTheme.typography.paragraph2,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary, // Ganti dengan warna yang diinginkan
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline // Ganti dengan warna untuk border saat tidak fokus
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
                                contentDescription = if (passwordVisible) "Sembunyikan password" else "Tampilkan password",
                                tint = MaterialTheme.colorScheme.primary// Warna sesuai tema
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(30.dp),
                    textStyle = AppTheme.typography.paragraph2,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { viewModel.onRememberMeChange(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary, // Warna ketika Checkbox terpilih
                            uncheckedColor = MaterialTheme.colorScheme.outline, // Warna ketika Checkbox tidak terpilih
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary // Warna checkmark
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
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = isFormValid && uiState !is LoginState.Loading
                ) {
                    if (uiState is LoginState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Masuk", color = MaterialTheme.colorScheme.onPrimary, style = AppTheme.typography.paragraph1)
                    }
                }

                if (uiState is LoginState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (uiState as LoginState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = AppTheme.typography.paragraph2
                    )
                }
            }
        }
    }
}



