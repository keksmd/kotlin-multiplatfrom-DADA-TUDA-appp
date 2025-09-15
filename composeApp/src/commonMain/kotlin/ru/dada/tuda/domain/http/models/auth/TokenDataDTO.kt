package ru.dada.tuda.domain.http.models.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenDataDTO(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("expires_in") val expiresIn: Int = 0,
    @SerialName("refresh_expires_in") val refreshExpiresIn: Int = 0,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("not-before-policy") val notBeforePolicy: Int = 0,
    @SerialName("session_state") val sessionState: String? = null,
    @SerialName("scope") val scope: String? = null
)
