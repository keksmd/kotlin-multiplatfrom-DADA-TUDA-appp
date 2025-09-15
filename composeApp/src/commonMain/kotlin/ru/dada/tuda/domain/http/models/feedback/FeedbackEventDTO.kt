package ru.dada.tuda.domain.http.models.feedback

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedbackEventDTO(
    @SerialName("eventId") val eventId: String?,
    @SerialName("like") val like: Boolean,
    @SerialName("viewedSeconds") val viewedSeconds: Int,
    @SerialName("moreOpened") val moreOpened: Boolean,
    @SerialName("reported") val reported: Boolean,
    val starred: Boolean,
    val referralLinkOpened: Boolean
)
