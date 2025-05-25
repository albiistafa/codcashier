package dev.codcow.kasirku.features.auth.ui.screens

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.codcow.kasirku.core.data.model.login.LoginRequest
import dev.codcow.kasirku.core.domain.model.AuthResult
import dev.codcow.kasirku.core.domain.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    @ApplicationContext private val context: Context

) : ViewModel() {

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun onPhoneNumberChange(newPhoneNumber: String) {
        _phoneNumber.value = newPhoneNumber
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }


    fun onRememberMeChange(isChecked: Boolean) {
        _rememberMe.value = isChecked
    }


    fun hasValidToken(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token == null) {
            return false
        }

        if (isSessionExpired(context)) {
            return false
        }
        return true
    }

    fun isSessionExpired(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val loginTime = sharedPreferences.getLong("login_time", -1)

        if (loginTime == -1L) {
            return true  // Tidak ada waktu login, anggap session expired
        }

        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - loginTime
        return timeDifference > 24 * 60 * 60 * 1000  // 24 jam dalam milidetik
    }

    fun loadSavedCredentials(context: Context, viewModel: LoginViewModel) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        if (isSessionExpired(context)) {
            with(sharedPreferences.edit()) {
                remove("auth_token")
                remove("login_time")
                remove("user_role")
                apply()
            }
            // JANGAN hapus phone_number, password, remember_me
        }

        val isRememberMe = sharedPreferences.getBoolean("remember_me", false)

        if (isRememberMe) {
            val savedPhoneNumber = sharedPreferences.getString("phone_number", "")
            val savedPassword = sharedPreferences.getString("password", "")

            if (!savedPhoneNumber.isNullOrEmpty()) {
                _phoneNumber.value = savedPhoneNumber
            }
            if (!savedPassword.isNullOrEmpty()) {
                _password.value = savedPassword
            }
            _rememberMe.value = true
        } else {
            _phoneNumber.value = ""
            _password.value = ""
            _rememberMe.value = false
        }
    }




    fun saveToken(context: Context, token: String, role: String) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("auth_token", token)
            putString("user_role", role)
            putLong("login_time", System.currentTimeMillis())// Simpan token
            apply()
        }
    }

    fun getUserRole(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_role", null)
    }


    fun login() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val result = loginRepository.login(LoginRequest(_phoneNumber.value, _password.value))

            _loginState.value = if (result.isSuccess) {
                val authResult = result.getOrNull()

                if (authResult != null) {
                    Log.d("LoginViewModel", "Login Berhasil! AuthResult: $authResult, Status: 200")
                    Log.d("LoginViewModel", "Role to save: ${authResult.role}")
                    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            if (_rememberMe.value) {
                                putString("phone_number", _phoneNumber.value)
                                putString("password", _password.value)
                                putBoolean("remember_me", true)
                            } else {
                                remove("phone_number")
                                remove("password")
                                putBoolean("remember_me", false)
                            }
                            apply()
                        }
                        apply()
                    }
                    saveToken(context, authResult.token, authResult.role)
                    LoginState.Success(authResult, 200)
                } else {
                    LoginState.Error("Unexpected empty response")
                }
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun onLoginClick() {
        login()
    }

    fun logout() {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("auth_token")
            remove("login_time")
            apply()
        }

        // Setelah logout, reset state ViewModel
        _rememberMe.value = false
        _loginState.value = LoginState.Idle

        Log.d("LoginViewModel", "Logout berhasil! Semua data user dibersihkan.")
    }

}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val data: AuthResult, val statusCode: Int = 200) : LoginState()
    data class Error(val message: String) : LoginState()
}
