package ru.dada.tuda.domain.repository.impl

import io.ktor.http.ContentType
import io.ktor.http.parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ru.dada.tuda.domain.http.models.auth.TokenDataDTO
import ru.dada.tuda.domain.http.models.auth.UserAuthDTO
import ru.dada.tuda.domain.repository.AuthRepository
import ru.dada.tuda.domain.util.AppError
import ru.dada.tuda.domain.util.ErrorHandler
import ru.dada.tuda.domain.util.KmpLog
import ru.dada.tuda.domain.util.Postman
import ru.dada.tuda.domain.util.Resource
import ru.dada.tuda.domain.util.UrlWorker

class AuthRepositoryImpl(
    private val postman: Postman,
    private val urlWorker: UrlWorker,
    private val errorHandler: ErrorHandler
) : AuthRepository {
    private val _userLiveData = MutableStateFlow<Resource<UserAuthDTO>?>(null)
    override val userLiveData: StateFlow<Resource<UserAuthDTO>?> = _userLiveData.asStateFlow()

    private val _tokenLiveData = MutableStateFlow<Resource<TokenDataDTO>>(Resource.Loading())
    override val tokenLiveData: StateFlow<Resource<TokenDataDTO>> = _tokenLiveData.asStateFlow()

    override suspend fun authentication(login: String, password: String, email: String) {
        KmpLog.d("AuthRepositoryImpl", "authentication")
        _userLiveData.value = Resource.Loading()

        val userRequest = mapOf(
            "username" to login,
            "password" to password,
            "email" to email
        )

        withContext(Dispatchers.IO) {
            try {
                val result = postman.post<String>(
                    baseUrl = urlWorker.baseUrl,
                    route = urlWorker.createNewUserRoute(),
                    body = userRequest
                )

                result.onSuccess {
                    _userLiveData.value = Resource.Success(UserAuthDTO(login, password))
                }.onError { error ->
                    val appError =
                        AppError.ServerError(error.status, error.message)
                    errorHandler.logError(appError)
                    _userLiveData.value = Resource.Error(errorHandler.handleError(appError))
                }
            } catch (e: Exception) {
                val appError = AppError.UnknownError(e)
                errorHandler.logError(appError)
                _userLiveData.value = Resource.Error(errorHandler.handleError(appError))
            }
        }
    }

    override suspend fun authorization(login: String, password: String) {
        KmpLog.d("AuthRepositoryImpl", "authorization")
        _userLiveData.value = Resource.Success(UserAuthDTO(login, password))
    }

    override suspend fun register(
        nickname: String,
        email: String,
        password: String
    ) {
        val userRequest = mapOf(
            "username" to nickname,
            "password" to password,
            "email" to email
        )
        withContext(Dispatchers.IO) {
            try {
                val result = postman.post<String>(
                    baseUrl = urlWorker.baseUrl,
                    route = urlWorker.createNewUser(),
                    body = userRequest
                )

                result.onSuccess {
                    _userLiveData.value = Resource.Success(UserAuthDTO(nickname, password))
                }.onError { error ->
                    _userLiveData.value = Resource.Error(error.message)
                }
            } catch (e: Exception) {
                _userLiveData.value = Resource.Error("Authentication error: ${e.message}")
            }
        }
    }

    override suspend fun getToken(username: String, password: String) {
        KmpLog.d("AuthRepositoryImpl", "getToken")

        withContext(Dispatchers.IO) {
            try {
                val result = postman.submitForm<TokenDataDTO>(
                    baseUrl = urlWorker.authUrl,
                    route = urlWorker.getTokenForLoginRoute(),
                    parameters = parameters {
                        append("username", username)
                        append("password", password)
                        append("grant_type", "password")
                        append("client_secret", "qYz5m2pnIQAW1dWjqzPsRirfD3rdYGh3")
                        append("client_id", "service-client")
                    },
                    contentType = ContentType.Application.FormUrlEncoded
                )

                _tokenLiveData.value = result
            } catch (e: Exception) {
                _tokenLiveData.value = Resource.Error("Token error: ${e.message}")
            }
        }
    }
}