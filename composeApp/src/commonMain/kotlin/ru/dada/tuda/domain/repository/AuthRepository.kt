package ru.dada.tuda.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.dada.tuda.domain.http.models.auth.TokenDataDTO
import ru.dada.tuda.domain.http.models.auth.UserAuthDTO
import ru.dada.tuda.domain.util.Resource

interface AuthRepository {
    val userLiveData: StateFlow<Resource<UserAuthDTO>?>
    val tokenLiveData: StateFlow<Resource<TokenDataDTO>>

    suspend fun authentication(login: String, password: String, email: String)
    suspend fun authorization(login: String, password: String)
    suspend fun register(nickname: String, email: String, password: String)
    suspend fun getToken(username: String, password: String)
}