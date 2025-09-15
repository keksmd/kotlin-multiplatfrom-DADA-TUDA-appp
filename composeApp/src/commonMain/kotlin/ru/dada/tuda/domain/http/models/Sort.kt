package ru.dada.tuda.domain.http.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Sort(
    @SerialName("empty") val empty: Boolean = false,
    @SerialName("unsorted") val unsorted: Boolean = false,
    @SerialName("sorted") val sorted: Boolean = false
)