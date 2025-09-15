package ru.dada.tuda.domain.http.models.shortlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.dada.tuda.domain.http.models.Pageable
import ru.dada.tuda.domain.http.models.Sort

@Serializable
data class ShortListDTO(
    @SerialName("content") val content: List<ShortlistEventDTO>? = null,
    @SerialName("pageable") val pageable: Pageable? = null,
    @SerialName("last") val last: Boolean = false,
    @SerialName("totalElements") val totalElements: Int = 0,
    @SerialName("totalPages") val totalPages: Int = 0,
    @SerialName("first") val first: Boolean = false,
    @SerialName("size") val size: Int = 0,
    @SerialName("number") val number: Int = 0,
    @SerialName("sort") val sort: List<String> = emptyList(),
    @SerialName("numberOfElements") val numberOfElements: Int = 0,
    @SerialName("empty") val empty: Boolean = false
)
