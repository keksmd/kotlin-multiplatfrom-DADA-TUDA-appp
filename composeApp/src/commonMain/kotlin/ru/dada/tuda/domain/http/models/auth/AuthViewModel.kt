package ru.dada.tuda.domain.http.models.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.dada.tuda.domain.repository.AuthRepository
import ru.dada.tuda.domain.util.TokenManager

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    val userLiveData = authRepository.userLiveData
    val tokenLiveData = authRepository.tokenLiveData

    private val _nickname = mutableStateOf("string12345")
    val nickname: State<String> = _nickname

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("string123")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _isPasswordVisible = mutableStateOf(false)
    val isPasswordVisible: State<Boolean> = _isPasswordVisible

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun updateLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun updateNickname(newNickname: String) {
        _nickname.value = newNickname
    }

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun updateConfirmPassword(newPassword: String) {
        _confirmPassword.value = newPassword
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun login() {
        if (_nickname.value.isBlank() || _password.value.isBlank()) {
            // TODO: Показать ошибку пользователю
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                authorization(_nickname.value, _password.value) // Выполняем авторизацию
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginWithGoogle() {
        _isLoading.value = true

        // TODO: Реализовать логику входа через Google

        _isLoading.value = false
    }

    fun authentication(login: String, password: String, email: String) {
        viewModelScope.launch {
            authRepository.authentication(login, password, email)
        }
    }

    fun authorization(login: String, password: String) {
        viewModelScope.launch {
            authRepository.authorization(login, password)
        }
    }

    fun register(nickname: String, email: String, password: String) {
        viewModelScope.launch {
            authRepository.register(nickname, email, password)
        }
    }

    fun getToken(username: String, password: String) {
        viewModelScope.launch {
            authRepository.getToken(username, password)
        }
    }

    fun saveToken(token: String) {
        tokenManager.saveToken(token)
    }
}