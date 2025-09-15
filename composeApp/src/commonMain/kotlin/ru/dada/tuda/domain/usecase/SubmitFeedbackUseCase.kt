package ru.dada.tuda.domain.usecase

import ru.dada.tuda.domain.http.models.feedback.FeedbackEventDTO
import ru.dada.tuda.domain.repository.EventRepository
import ru.dada.tuda.domain.repository.FeedbackRepository
import ru.dada.tuda.domain.util.KmpLog
import ru.dada.tuda.domain.util.Resource

class SubmitFeedbackUseCase(
    private val feedbackRepository: FeedbackRepository,
    private val eventRepository: EventRepository
) {
    val resultLiveData = feedbackRepository.feedbackResultLiveData
    val feedbackEventLiveData = feedbackRepository.feedbackEventDTOLiveData

    suspend fun execute(feedbackEventDTO: FeedbackEventDTO) {
        feedbackRepository.submitFeedback(feedbackEventDTO)
    }

    suspend fun loadNewCardsBasedOnCurrentCount() {
        val currentCardsResource = eventRepository.cardsLiveData.value

        if (currentCardsResource is Resource.Success) {
            val currentCards = currentCardsResource.data

            currentCards.let { cards ->
                if (cards.size < 2) {
                    eventRepository.loadExactNumberOfCards(10)
                    KmpLog.d("SubmitFeedbackUseCase", "Loading 10 new cards")
                } else {
                    eventRepository.loadExactNumberOfCards(1)
                    KmpLog.d("SubmitFeedbackUseCase", "Loading 1 new card")
                }
                KmpLog.d("SubmitFeedbackUseCase", "Get new event")
            }
        } else {
            KmpLog.d("SubmitFeedbackUseCase", "Current cards resource is not Success state")
        }
    }
}
