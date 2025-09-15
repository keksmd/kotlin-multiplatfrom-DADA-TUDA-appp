package ru.dada.tuda.domain.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ru.dada.tuda.domain.http.models.feedback.FeedbackEventDTO
import ru.dada.tuda.domain.repository.FeedbackRepository
import ru.dada.tuda.domain.util.Postman
import ru.dada.tuda.domain.util.Resource
import ru.dada.tuda.domain.util.UrlWorker

class FeedbackRepositoryImpl(
    private val postman: Postman,
    private val urlWorker: UrlWorker
) : FeedbackRepository {
    private val _feedbackResultLiveData = MutableStateFlow<Resource<String>>(Resource.Loading())
    override val feedbackResultLiveData: StateFlow<Resource<String>> = _feedbackResultLiveData.asStateFlow()

    private val _feedbackEventDTOLiveData = MutableStateFlow<Resource<FeedbackEventDTO>>(Resource.Loading())
    override val feedbackEventDTOLiveData: StateFlow<Resource<FeedbackEventDTO>> = _feedbackEventDTOLiveData.asStateFlow()

    override suspend fun submitFeedback(feedbackEventDTO: FeedbackEventDTO) {
        _feedbackEventDTOLiveData.value = Resource.Success(feedbackEventDTO)
        _feedbackResultLiveData.value = Resource.Loading()

        withContext(Dispatchers.IO) {
            val result = postman.post<String>(
                baseUrl = urlWorker.baseUrl,
                route = urlWorker.getFeedbackRoute(),
                body = feedbackEventDTO,
                headers = mapOf(
                    "Authorization" to "Bearer ${urlWorker.getAuthToken()}",
                    "Content-Type" to "application/json"
                )
            )
            _feedbackResultLiveData.value = result
        }
    }
}
