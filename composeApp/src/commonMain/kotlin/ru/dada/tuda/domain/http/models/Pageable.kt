package ru.dada.tuda.domain.http.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pageable(
    @SerialName("pageNumber") val pageNumber: Int = 0,
    @SerialName("pageSize") val pageSize: Int = 0,
    @SerialName("sort") val sort: List<String> = emptyList(),
    @SerialName("offset") val offset: Int = 0,
    @SerialName("paged") val paged: Boolean = false,
    @SerialName("unpaged") val unpaged: Boolean = false
)