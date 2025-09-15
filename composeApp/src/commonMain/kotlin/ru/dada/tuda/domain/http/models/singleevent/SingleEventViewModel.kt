package ru.dada.tuda.domain.http.models.singleevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.dada.tuda.domain.repository.EventRepository
import ru.dada.tuda.domain.repository.ShortlistRepository
import ru.dada.tuda.domain.util.Resource
import ru.dada.tuda.domain.http.models.CardItem

class SingleEventViewModel(
    private val shortlistRepository: ShortlistRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _eventLiveData = MutableStateFlow<Resource<CardItem>?>(Resource.Loading())
    val eventLiveData: StateFlow<Resource<CardItem>?> = _eventLiveData

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _canNavigatePrevious = MutableStateFlow(false)
    val canNavigatePrevious: StateFlow<Boolean> = _canNavigatePrevious.asStateFlow()

    private val _canNavigateNext = MutableStateFlow(false)
    val canNavigateNext: StateFlow<Boolean> = _canNavigateNext.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private var currentEventId: String? = null
    private var currentCardItem: CardItem? = null
    private var currentIndex: Int = -1
    
    // Получаем список событий из репозитория
    private val shortlistEvents: List<CardItem>
        get() = shortlistRepository.getCurrentShortlistItems()

    fun loadEvent(eventId: String) {
        if (currentEventId == eventId && _eventLiveData.value is Resource.Success) {
            return
        }

        currentEventId = eventId
        _isLoading.value = true

        viewModelScope.launch {
            try {
                _eventLiveData.value = Resource.Loading()

                val result = eventRepository.getEventById(eventId)

                when (result) {
                    is Resource.Success -> {
                        currentCardItem = result.data
                        _eventLiveData.value = result
                        _isFavorite.value = result.data?.starred ?: false
                        _isLiked.value = result.data?.like ?: false
                    }
                    is Resource.Error -> {
                        _eventLiveData.value = result
                    }
                    is Resource.Loading -> {
                        // Already set to loading above
                    }
                    else -> {
                        _eventLiveData.value = Resource.Error("Неизвестная ошибка")
                    }
                }

            } catch (e: Exception) {
                _eventLiveData.value = Resource.Error("Ошибка загрузки: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadEventFromCardItem(cardItem: CardItem) {
        currentEventId = cardItem.id
        currentCardItem = cardItem
        _isLoading.value = false

        _eventLiveData.value = Resource.Success(cardItem)
        _isFavorite.value = cardItem.starred
        _isLiked.value = cardItem.like
    }

    fun setShortlistContext(events: List<CardItem>, currentEventId: String) {
        // Находим индекс текущего события в списке репозитория
        currentIndex = shortlistEvents.indexOfFirst { it.id == currentEventId }
        updateNavigationState()

        // Предзагрузка следующих страниц если осталось мало событий
        if (currentIndex >= 0 && currentIndex >= shortlistEvents.size - 2) {
            viewModelScope.launch {
                try {
                    shortlistRepository.loadNextPage()
                } catch (e: Exception) {
                    // Игнорируем ошибки предзагрузки
                }
            }
        }
    }

    private fun updateNavigationState() {
        _canNavigatePrevious.value = currentIndex > 0
        _canNavigateNext.value = currentIndex >= 0 && currentIndex < shortlistEvents.size - 1
    }

    fun navigateToPrevious(): CardItem? {
        if (currentIndex <= 0) return null

        currentIndex--
        val previousEvent = shortlistEvents[currentIndex]
        loadEventFromCardItem(previousEvent)
        updateNavigationState()
        return previousEvent
    }

    fun navigateToNext(): CardItem? {
        if (currentIndex < 0 || currentIndex >= shortlistEvents.size - 1) return null

        currentIndex++
        val nextEvent = shortlistEvents[currentIndex]
        loadEventFromCardItem(nextEvent)
        updateNavigationState()

        // Предзагрузка если близко к концу
        if (currentIndex >= shortlistEvents.size - 2) {
            viewModelScope.launch {
                try {
                    shortlistRepository.loadNextPage()
                } catch (e: Exception) {
                    // Игнорируем ошибки предзагрузки
                }
            }
        }

        return nextEvent
    }

    fun toggleFavorite() {
        val currentCard = currentCardItem ?: return

        viewModelScope.launch {
            try {
                val newFavoriteState = !_isFavorite.value

                _isFavorite.value = newFavoriteState

                val updatedCard = currentCard.copy(starred = newFavoriteState)
                currentCardItem = updatedCard
                _eventLiveData.value = Resource.Success(updatedCard)

                // Обновляем событие в репозитории
                shortlistRepository.updateItemFavoriteStatus(currentCard.id, newFavoriteState)

            } catch (e: Exception) {
                _isFavorite.value = !_isFavorite.value

                val revertedCard = currentCard.copy(starred = _isFavorite.value)
                currentCardItem = revertedCard
                _eventLiveData.value = Resource.Success(revertedCard)
            }
        }
    }

    fun toggleLike() {
        val currentCard = currentCardItem ?: return

        viewModelScope.launch {
            try {
                val newLikeState = !_isLiked.value

                _isLiked.value = newLikeState

                val updatedCard = currentCard.copy(like = newLikeState)
                currentCardItem = updatedCard
                _eventLiveData.value = Resource.Success(updatedCard)

                if (newLikeState) {
                    // Показываем диалог удаления при лайке
                    _showDeleteDialog.value = true
                }

            } catch (e: Exception) {
                _isLiked.value = !_isLiked.value

                val revertedCard = currentCard.copy(like = _isLiked.value)
                currentCardItem = revertedCard
                _eventLiveData.value = Resource.Success(revertedCard)
            }
        }
    }

    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun deleteEvent() {
        viewModelScope.launch {
            try {
                val currentCard = currentCardItem ?: return@launch

                // Удаляем событие из репозитория
                shortlistRepository.removeItem(currentCard.id)
                
                // Обновляем текущий список и индекс
                val updatedEvents = shortlistEvents
                
                // Если удалили последний элемент, переходим к предыдущему
                if (currentIndex >= updatedEvents.size && updatedEvents.isNotEmpty()) {
                    currentIndex--
                }

                // Загружаем следующее событие или завершаем
                if (updatedEvents.isNotEmpty() && currentIndex >= 0) {
                    val nextEvent = updatedEvents[currentIndex]
                    loadEventFromCardItem(nextEvent)
                    updateNavigationState()
                } else {
                    // Список пуст, можно закрыть экран или показать пустое состояние
                    _eventLiveData.value = Resource.Empty()
                }

                _showDeleteDialog.value = false

            } catch (e: Exception) {
                // Обработка ошибки удаления
                _showDeleteDialog.value = false
            }
        }
    }

    fun refreshEvent() {
        currentEventId?.let { eventId ->
            loadEvent(eventId)
        }
    }

    fun getCurrentCardItem(): CardItem? {
        return currentCardItem
    }
}
