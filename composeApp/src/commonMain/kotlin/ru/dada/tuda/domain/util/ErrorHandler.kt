package ru.dada.tuda.domain.util

/**
 * Типы ошибок в приложении
 */
sealed class AppError {
    data class NetworkError(val code: Int, val message: String) : AppError()
    data class ServerError(val code: Int, val message: String) : AppError()
    data class UnauthorizedError(val message: String) : AppError()
    data class ValidationError(val field: String, val message: String) : AppError()
    data class UnknownError(val throwable: Throwable) : AppError()
}

/**
 * Интерфейс для обработки ошибок
 */
interface ErrorHandler {
    /**
     * Преобразует ошибку в человекочитаемое сообщение
     */
    fun handleError(error: AppError): String
    
    /**
     * Логирует ошибку
     */
    fun logError(error: AppError)
    
    /**
     * Определяет, нужно ли показывать ошибку пользователю
     */
    fun shouldShowToUser(error: AppError): Boolean
}

/**
 * Реализация обработчика ошибок
 */
class ErrorHandlerImpl : ErrorHandler {
    override fun handleError(error: AppError): String {
        return when (error) {
            is AppError.NetworkError -> {
                when (error.code) {
                    0 -> "Отсутствует подключение к интернету"
                    in 400..499 -> "Ошибка запроса: ${error.message}"
                    in 500..599 -> "Сервер временно недоступен. Попробуйте позже"
                    else -> "Проблема с сетью: ${error.message}"
                }
            }
            is AppError.ServerError -> {
                "Ошибка сервера (${error.code}): ${error.message}"
            }
            is AppError.UnauthorizedError -> {
                "Необходима авторизация. Пожалуйста, войдите в систему"
            }
            is AppError.ValidationError -> {
                "Проверьте правильность заполнения поля: ${error.field}"
            }
            is AppError.UnknownError -> {
                "Произошла ошибка: ${error.throwable.message ?: "Неизвестная ошибка"}"
            }
        }
    }
    
    override fun logError(error: AppError) {
        when (error) {
            is AppError.NetworkError -> {
                KmpLog.e("ErrorHandler", "Network error ${error.code}: ${error.message}")
            }
            is AppError.ServerError -> {
                KmpLog.e("ErrorHandler", "Server error ${error.code}: ${error.message}")
            }
            is AppError.UnauthorizedError -> {
                KmpLog.e("ErrorHandler", "Unauthorized: ${error.message}")
            }
            is AppError.ValidationError -> {
                KmpLog.e("ErrorHandler", "Validation error in ${error.field}: ${error.message}")
            }
            is AppError.UnknownError -> {
                KmpLog.e("ErrorHandler", "Unknown error: ${error.throwable.message}")
                // В production отправляем в crash reporting
                // FirebaseCrashlytics.getInstance().recordException(error.throwable)
            }
        }
    }
    
    override fun shouldShowToUser(error: AppError): Boolean {
        return when (error) {
            is AppError.NetworkError -> true
            is AppError.ServerError -> error.code >= 500 // Показываем только серверные ошибки
            is AppError.UnauthorizedError -> true
            is AppError.ValidationError -> true
            is AppError.UnknownError -> false // Не показываем неизвестные ошибки
        }
    }
}

/**
 * Расширение для Resource для упрощения обработки ошибок
 */
/**
 * Расширение для Resource для упрощения обработки ошибок
 */
fun <T> Resource<T>.mapError(errorHandler: ErrorHandler): Resource<T> {
    return when (this) {
        is Resource.Error -> {
            // Попытаться определить тип ошибки по сообщению или статусу
            val appError = when {
                this.status == 401 || this.status == 403 -> AppError.UnauthorizedError(this.message)
                this.status in 400..499 -> AppError.NetworkError(this.status, this.message)
                this.status >= 500 -> AppError.ServerError(this.status, this.message)
                else -> AppError.UnknownError(Exception(this.message))
            }
            
            errorHandler.logError(appError)
            val userMessage = errorHandler.handleError(appError)
            Resource.Error(userMessage, this.status)
        }
        else -> this
    }
}
