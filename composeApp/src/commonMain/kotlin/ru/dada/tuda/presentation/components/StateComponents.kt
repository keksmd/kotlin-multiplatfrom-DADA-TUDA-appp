package ru.dada.tuda.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Индикатор загрузки с анимированным текстом
 */
@Composable
fun LoadingIndicator(
    message: String = "Загрузка...",
    modifier: Modifier = Modifier
) {
    var dots by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            dots = when (dots.length) {
                0 -> "."
                1 -> ".."
                2 -> "..."
                else -> ""
            }
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$message$dots",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.widthIn(min = 100.dp)
            )
        }
    }
}

/**
 * Компактный индикатор загрузки для кнопок
 */
@Composable
fun ButtonLoadingIndicator(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier.size(20.dp),
        strokeWidth = 2.dp,
        color = MaterialTheme.colorScheme.onPrimary
    )
}

/**
 * Расширенное представление ошибки с иконкой и действиями
 */
@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Error,
    retryText: String = "Повторить"
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(retryText)
            }
        }
    }
}

/**
 * Представление пустого состояния
 */
@Composable
fun EmptyView(
    message: String,
    icon: ImageVector = Icons.Filled.Search,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(96.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedButton(onClick = onAction) {
                    Text(actionText)
                }
            }
        }
    }
}

/**
 * Кнопка с состоянием загрузки
 */
@Composable
fun LoadingButton(
    onClick: () -> Unit,
    text: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = isLoading,
            label = "button_loading"
        ) { loading ->
            if (loading) {
                ButtonLoadingIndicator()
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text)
                }
            }
        }
    }
}

/**
 * Индикатор успешного действия с анимацией
 */
@Composable
fun SuccessIndicator(
    message: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000)
        onDismiss()
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
