package ru.dada.tuda.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.dada.tuda.domain.http.models.feedback.FeedbackEventDTO
import ru.dada.tuda.domain.util.Resource

interface FeedbackRepository {
    val feedbackResultLiveData: StateFlow<Resource<String>>
    val feedbackEventDTOLiveData: StateFlow<Resource<FeedbackEventDTO>>

    suspend fun submitFeedback(feedbackEventDTO: FeedbackEventDTO)
}