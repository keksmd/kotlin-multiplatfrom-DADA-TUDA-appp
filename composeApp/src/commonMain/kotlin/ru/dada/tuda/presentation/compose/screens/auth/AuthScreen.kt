package ru.dada.tuda.presentation.compose.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dadatuda.composeapp.generated.resources.Res
import dadatuda.composeapp.generated.resources.ic_revert_icon_thin
import org.jetbrains.compose.resources.painterResource
import ru.dada.tuda.domain.http.models.auth.AuthViewModel
import org.koin.compose.koinInject
import ru.dada.tuda.presentation.theme.HeadlineMediumText

@Composable
fun AuthScreen(
    navigateToRegistration: () -> Unit,
    navigateToMain: () -> Unit,
    viewModel: AuthViewModel = koinInject()
) {

    // Наблюдаем за LiveData
    val userLiveData by viewModel.userLiveData.collectAsState()
    val tokenLiveData by viewModel.tokenLiveData.collectAsState()

    // Локальное состояние для отслеживания загрузки
    val isLoading by viewModel.isLoading

    // Обработка результата получения пользователя
    LaunchedEffect(userLiveData) {
        userLiveData?.let { result ->
            result.onSuccess { userResponse ->
                viewModel.updateLoadingState(false)
                userResponse.data.let { userData ->
                    // TODO: необходимо сделать валидацию данных
                    viewModel.getToken(userData.login.orEmpty(), userData.password.orEmpty())
                }
            }.onLoading {
                viewModel.updateLoadingState(true)
            }.onError { error ->
                viewModel.updateLoadingState( false)
//                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Обработка результата получения токена
    LaunchedEffect(tokenLiveData) {
        tokenLiveData.let { result ->
            result.onSuccess { tokenResponse ->
                viewModel.updateLoadingState(false)
                viewModel.saveToken(tokenResponse.data.accessToken.orEmpty())
                navigateToMain()
            }.onError { error ->
                viewModel.updateLoadingState(false)
//                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
    ) {
        // Title
        Spacer(Modifier.height(96.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(
                painterResource(Res.drawable.ic_revert_icon_thin),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
            HeadlineMediumText(text = "Вход в аккаунт", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Nickname input
        TextField(
            value = viewModel.nickname.value,
            onValueChange = viewModel::updateNickname,
            placeholder = {
                Text(
                    text = "Ник",
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFFCCFF00),
                unfocusedIndicatorColor = Color.Gray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = viewModel.password.value,
            onValueChange = viewModel::updatePassword,
            placeholder = {
                Text(
                    text = "Пароль",
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFFCCFF00),
                unfocusedIndicatorColor = Color.Gray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            visualTransformation = if (viewModel.isPasswordVisible.value)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(
                        imageVector = if (viewModel.isPasswordVisible.value)
                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility",
                        tint = Color.Gray
                    )
                }
            },
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.weight(1f))

        // Login button
        Button(
            onClick = viewModel::login,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEFEF)),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(text = "Войти", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google login button
        Button(
            onClick = viewModel::loginWithGoogle,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2FF87)),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(text = "Авторизация с помощью Google", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google login button
        Button(
            onClick = viewModel::loginWithGoogle,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2FF87)),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(text = "Авторизация с помощью Yandex", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Registration text
        Row(Modifier.padding(bottom = 64.dp)) {
            Text(
                text = "у вас ещё нет аккаунта? ",
                color = Color.Black,
                fontSize = 14.sp
            )
            Text(
                text = "Регистрация",
                color = Color.Black,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = navigateToRegistration),
                textDecoration = TextDecoration.Underline
            )
        }
    }
}