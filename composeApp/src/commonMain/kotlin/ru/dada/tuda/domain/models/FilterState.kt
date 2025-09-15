package ru.dada.tuda.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class FilterState(
    val categories: List<String> = emptyList(),
    val startDateTime: String? = null,
    val endDateTime: String? = null,
    val minPrice: String? = null,
    val maxPrice: String? = null,
    val search: String = "",
    val isStarred: Boolean = false
)