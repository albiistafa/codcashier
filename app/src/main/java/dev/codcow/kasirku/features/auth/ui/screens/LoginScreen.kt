package dev.codcow.kasirku.features.auth.ui.screens
import dev.codcow.kasirku.ui.components.readableWidth
import androidx.compose.ui.platform.LocalConfiguration

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import dev.codcow.kasirku.ui.components.StrukMark
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
            // Pemilik ke Beranda, pegawai langsung ke Kasir
            val start = if (viewModel.getUserRole(context) == "admin") "home" else "menu"
            navController.navigate(start) {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    val loading = uiState is LoginState.Loading
    fun submit() { if (isFormValid && !loading) viewModel.onLoginClick() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.hero.first())
            .imePadding()
    ) {
        // Same mark and name as the opening animation. On a short window (phone in landscape)
        // only the name stays, so the form keeps most of the height.
        val roomy = LocalConfiguration.current.screenHeightDp >= 560
        Column(
            Modifier.fillMaxWidth().statusBarsPadding().padding(top = if (roomy) 48.dp else 12.dp, bottom = if (roomy) 32.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (roomy) StrukMark()
            Text("Kasirku", style = AppTheme.typography.heading2Bold, color = AppTheme.colors.onHero)
            if (roomy) Text(
                "Masuk dengan nomor telepon dan password akunmu",
                style = AppTheme.typography.paragraph1,
                color = AppTheme.colors.onHero.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .readableWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                // The form scrolls on its own when the keyboard is up; the mark stays put
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Masuk", style = AppTheme.typography.heading4Bold, color = MaterialTheme.colorScheme.onSurface)
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { viewModel.onPhoneNumberChange(it) },
                label = { Text("Nomor telepon") },
                placeholder = { Text("Contoh: 081234567890") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                isError = uiState is LoginState.Error,
                singleLine = true,
                textStyle = AppTheme.typography.paragraph1,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Sembunyikan password" else "Tampilkan password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                isError = uiState is LoginState.Error,
                singleLine = true,
                textStyle = AppTheme.typography.paragraph1,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = rememberMe, role = Role.Checkbox, onValueChange = { viewModel.onRememberMeChange(it) })
            ) {
                Checkbox(checked = rememberMe, onCheckedChange = null)
                Spacer(Modifier.width(8.dp))
                Text("Ingat saya di HP ini", style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.onSurface)
            }
            if (uiState is LoginState.Error) {
                // What went wrong and what to do: wrong password, unknown number, or no connection
                Text(
                    text = (uiState as LoginState.Error).message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = AppTheme.typography.paragraph2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
            Button(
                onClick = ::submit,
                enabled = isFormValid && !loading,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Sedang masuk…", style = AppTheme.typography.paragraph1Bold)
                } else {
                    Text("Masuk", style = AppTheme.typography.paragraph1Bold)
                }
            }
        }
    }
}
