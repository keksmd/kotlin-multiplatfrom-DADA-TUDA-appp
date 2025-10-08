package ru.dada.tuda.domain.http.models

import ru.dada.tuda.domain.util.DataTime
import kotlinx.serialization.Serializable
import ru.dada.tuda.domain.http.models.event.ApiResponseEventDTO
import ru.dada.tuda.domain.http.models.shortlist.ShortlistEventDTO
import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import ru.dada.tuda.domain.util.toFormattedDateTime
import kotlinx.serialization.Transient

@Immutable
@Serializable
data class CardItem(
    val id: String,
    val imageURL: List<String> = emptyList(),
    @SerialName("name")
    val title: String? = null,
    val description: String? = null,
    val city: String? = null,
    val address: String? = null,
    val locationName: String? = null,
    val tags: MutableList<String?>? = null,
    val categories: MutableList<String?>? = null,
    val shortDescription: String? = null,
    val price: String? = null,
    val priceType: String? = null,
    val type: String? = null,
    val date: String? = null, // Изменено с LocalDateTime на String
    val dateEnd: String? = null, // Изменено с LocalDateTime на String
    val referralLink: String? = null,
    val source: String? = null,
    val creatorId: String? = null,
    val views: Int = 0,
    val likes: Int = 0,
    val isFavorite: Boolean = false,
    val like: Boolean = false,
    val moreOpen: Boolean = false,
    val reported: Boolean = false,
    val starred: Boolean = false,
    val startSecond: Int = 0,
    val endSecond: Int = 0
) {
    companion object {
        private fun sanitizeList(list: MutableList<String?>?): MutableList<String?>? {
            return list?.map { item ->
                if (item != null && containsLettersAndDigits(item)) {
                    "Другое"
                } else {
                    item
                }
            }?.toMutableList()
        }

        private fun containsLettersAndDigits(str: String): Boolean {
            val hasLetter = str.any { it.isLetter() }
            val hasDigit = str.any { it.isDigit() }
            return hasLetter && hasDigit
        }
    }
    constructor(dto: ApiResponseEventDTO) : this(
        id = dto.id,
        imageURL = dto.imageURL.orEmpty().filterNotNull(),
        title = dto.name,
        description = dto.description,
        city = dto.city,
        address = dto.address,
        locationName = dto.locationName,
        tags = sanitizeList(dto.tags),
        categories = sanitizeList(dto.categories),
        shortDescription = dto.shortDescription,
        price = dto.price,
        priceType = dto.priceType,
        type = dto.type,
        date = dto.date, // Теперь String
        dateEnd = dto.dateEnd, // Теперь String
        referralLink = dto.referralLink,
        source = dto.source,
        creatorId = dto.creatorId,
        views = dto.views,
        likes = dto.likes,
        isFavorite = dto.isFavorite
    )

    constructor(dto: ShortlistEventDTO) : this(
        id = dto.id,
        imageURL = dto.imageURL.orEmpty().filterNotNull(),
        title = dto.name,
        description = dto.description,
        city = dto.city,
        address = dto.address,
        locationName = dto.locationName,
        tags = sanitizeList(dto.tags),
        categories = sanitizeList(dto.categories),
        shortDescription = dto.shortDescription,
        price = dto.price,
        priceType = dto.priceType,
        type = dto.type,
        date = dto.date,
        dateEnd = dto.dateEnd,
        referralLink = dto.referralLink,
        source = dto.source,
        creatorId = dto.creatorId,
        views = 0, // ShortlistEventDTO не содержит views
        likes = 0, // ShortlistEventDTO не содержит likes
        isFavorite = dto.isFavorite
    )

    @Transient
    val dateTime: DataTime? = date?.let { DataTime.parse(it) }

    fun getMainScreenDate() = dateTime?.let { "Ближайщее ${it.getDate()} ${it.getTime()}" } ?: "Не указана"

    fun getViewedSecond(): Int {
        return endSecond - startSecond
    }

    fun getFormattedDate(): String {
        return date?.toFormattedDateTime() ?: "Не указана"
    }
}