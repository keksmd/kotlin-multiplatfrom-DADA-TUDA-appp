package ru.dada.tuda.domain.http.models.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.dada.tuda.domain.usecase.SubmitFeedbackUseCase
import ru.dada.tuda.domain.http.models.CardItem
import ru.dada.tuda.presentation.ui.components.SwipeDirection
import ru.dada.tuda.domain.util.KmpLog
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class FeedbackViewModel(
    private val submitFeedbackUseCase: SubmitFeedbackUseCase
) : ViewModel() {

    val feedbackResultLiveData = submitFeedbackUseCase.resultLiveData
    val feedbackEventLiveData = submitFeedbackUseCase.feedbackEventLiveData

    fun setValue(cardItem: CardItem) {
        val eventId = cardItem.id
        val like = cardItem.like
        val viewedSeconds = cardItem.getViewedSecond()
        val moreOpened = cardItem.moreOpen
        val reported = cardItem.reported
        val starred = cardItem.starred // Assuming default value for starred
        val referralLinkOpened = false // Assuming default value for referralLinkOpened

        val feedbackEventDTO = FeedbackEventDTO(
            eventId, like, viewedSeconds, moreOpened, reported, starred, referralLinkOpened
        )

        viewModelScope.launch {
            submitFeedbackUseCase.execute(feedbackEventDTO)
        }
    }

    fun loadCards() {
        viewModelScope.launch {
            submitFeedbackUseCase.loadNewCardsBasedOnCurrentCount()
        }
    }

    fun handleCardSwiped(
        card: CardItem,
        direction: SwipeDirection
    ) {
        // Отправляем фидбек
        sendFeedback(
            card.copy(
                like = direction == SwipeDirection.RIGHT,
                endSecond = currentSecondOfMinute()
            )
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun currentSecondOfMinute(): Int =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).second

    fun sendFeedback(card: CardItem) {
        setValue(card)
        KmpLog.d("MainScreen", "Sending feedback for card: ${card.title}")
    }
}