package ru.dada.tuda.domain.http.models.shortlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShortlistEventDTO(
    @SerialName("city") val city: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("locationName") val locationName: String? = null,
    @SerialName("id") val id: String,
    @SerialName("tags") val tags: MutableList<String?>? = null,
    @SerialName("categories") val categories: MutableList<String?>? = null,
    @SerialName("shortDescription") val shortDescription: String? = null,
    @SerialName("imageURL") val imageURL: MutableList<String?>? = null,
    @SerialName("price") val price: String? = null,
    @SerialName("priceType") val priceType: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("dateEnd") val dateEnd: String? = null,
    @SerialName("referralLink") val referralLink: String? = null,
    @SerialName("source") val source: String? = null,
    @SerialName("creatorId") val creatorId: String? = null,
    @SerialName("starred") val isFavorite: Boolean = false,
    @SerialName("likes") val likes: Int = 0
)
