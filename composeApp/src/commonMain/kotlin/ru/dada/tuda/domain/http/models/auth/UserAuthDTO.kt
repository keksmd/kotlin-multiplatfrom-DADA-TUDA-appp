package ru.dada.tuda.domain.http.models.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAuthDTO(
    @SerialName("login") val login: String? = null,
    @SerialName("password") val password: String? = null
)