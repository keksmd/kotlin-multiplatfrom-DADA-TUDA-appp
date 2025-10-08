package ru.dada.tuda.domain.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.dada.tuda.domain.http.models.CardItem
import ru.dada.tuda.domain.http.models.event.ApiResponseEventDTO
import ru.dada.tuda.domain.http.models.filters.EventFilterParams
import ru.dada.tuda.domain.models.FilterState
import ru.dada.tuda.domain.repository.EventRepository
import ru.dada.tuda.domain.util.KmpLog
import ru.dada.tuda.domain.util.Postman
import ru.dada.tuda.domain.util.Resource
import ru.dada.tuda.domain.util.UrlWorker
import ru.dada.tuda.domain.util.ErrorHandler
import ru.dada.tuda.domain.util.AppError
import ru.dada.tuda.domain.util.mapError

class EventRepositoryImpl(
    private val postman: Postman,
    private val urlWorker: UrlWorker,
    private val errorHandler: ErrorHandler
) : EventRepository {
    private val _cardsLiveData =
        MutableStateFlow<Resource<MutableList<CardItem>>>(Resource.Loading())
    override val cardsLiveData: StateFlow<Resource<MutableList<CardItem>>> =
        _cardsLiveData.asStateFlow()

    // Filter state management - in-memory only
    private val _filtersFlow = MutableStateFlow(FilterState())
    override val filtersFlow: StateFlow<FilterState> = _filtersFlow.asStateFlow()

    // Pagination state
    private var currentPage = 0
    private var isLoading = false
    private var hasMorePages = true
    private val pageSize = 3

    // Последние фильтры для повторного использования (пейджинг и т.п.)
    private var lastFilterParams: EventFilterParams? = null

    companion object {
        private const val MAX_CARDS = 3
    }

    override suspend fun initializeCardsIfEmpty() {
        val current = cardsLiveData.value
        if (current is Resource.Empty || current is Resource.Error) {
            loadExactNumberOfCards(MAX_CARDS)
        }
        // Если уже Loading или Success — ничего не делаем, чтобы не дублировать запрос
    }

    override suspend fun loadExactNumberOfCards(count: Int) {
        if (isLoading) return

        isLoading = true
        _cardsLiveData.update { Resource.Loading() }
        currentPage = 0
        hasMorePages = true

        withContext(Dispatchers.IO) {
            try {
                val baseArgs = mutableMapOf<String, Any>(
                    "page" to currentPage,
                    "page_size" to count
                )
                // Прокидываем фильтры как query-параметры
                lastFilterParams?.getParams()?.forEach { (k, v) -> baseArgs[k] = v }

                val result = postman.get<MutableList<CardItem>>(
                    baseUrl = urlWorker.baseUrl,
                    route = urlWorker.getEventsRoute(),
                    arguments = baseArgs,
                    headers = mapOf(
                        "Authorization" to "Bearer ${urlWorker.getAuthToken()}"
                    )
                )
                _cardsLiveData.update { result.mapError(errorHandler) }
            } catch (e: Exception) {
                val error = AppError.UnknownError(e)
                errorHandler.logError(error)
                _cardsLiveData.update { Resource.Error(errorHandler.handleError(error)) }
            } finally {
                isLoading = false
            }
        }
    }

    override suspend fun reloadWithFilters(params: EventFilterParams) {
        lastFilterParams = params
        currentPage = 0
        hasMorePages = true
        loadExactNumberOfCards(MAX_CARDS)
    }

    override suspend fun removeCardAndLoadOne(position: Int) {
        val currentCards = _cardsLiveData.value.data?.toMutableList() ?: mutableListOf()
        if (position < currentCards.size) {
            val removedCard = currentCards.removeAt(position)
            _cardsLiveData.update { Resource.Success(currentCards) }

            KmpLog.d(
                "EventRepository",
                "Удалена карточка: ${removedCard.id}. Осталось: ${currentCards.size}"
            )

            // Загружаем новые карточки, если осталось мало
            if (currentCards.size < 5 && hasMorePages && !isLoading) {
                KmpLog.d("EventRepository", "Загружаем дополнительные карточки после удаления")
                loadMoreCards()
            }
        }
    }

    override fun addCard(card: CardItem?) {
//        _cardsLiveData.update {
//            val currentCards = it.data?.toMutableList() ?: mutableListOf()
//            currentCards.add(card)
//            println("Добавлена карточка: ${card?.id} ${currentCards.size}")
//            Resource.Success(currentCards)
//        }
    }

    override suspend fun loadMoreCards() {
        if (isLoading || !hasMorePages) return

        isLoading = true
        withContext(Dispatchers.IO) {
            try {
                val baseArgs = mutableMapOf<String, Any>(
                    "size" to pageSize,
                    "page" to currentPage
                )
                lastFilterParams?.getParams()?.forEach { (k, v) -> baseArgs[k] = v }

                val result = postman.get<ApiResponseEventDTO>(
                    baseUrl = urlWorker.baseUrl,
                    route = urlWorker.getEventsRoute(),
                    arguments = baseArgs,
                    headers = mapOf(
                        "Authorization" to "Bearer ${urlWorker.getAuthToken()}"
                    )
                )

                result.onSuccess { eventsData ->
                    // Simulate loading a batch of cards instead of just one
                    val currentCards = _cardsLiveData.value.data?.toMutableList() ?: mutableListOf()
                    repeat(minOf(pageSize, 3)) { index -> // Load up to 3 new cards
                        val newCard =
                            CardItem(eventsData.data.copy(id = "${eventsData.data.id}_page${currentPage}_$index"))
                        currentCards.add(newCard)
                    }
                    _cardsLiveData.update { Resource.Success(currentCards) }
                    currentPage++

                    // Check if we have more pages (simulate end condition)
                    hasMorePages = currentCards.size < MAX_CARDS
                    KmpLog.d(
                        "EventRepository",
                        "Добавлены новые карточки. Всего: ${currentCards.size}, hasMore: $hasMorePages"
                    )
                }.onError { error ->
                    KmpLog.e(
                        "EventRepository",
                        "Ошибка загрузки дополнительных событий: ${error.message}"
                    )
                }
            } catch (e: Exception) {
                KmpLog.e(
                    "EventRepository",
                    "Ошибка при загрузке дополнительных событий: ${e.message}"
                )
            } finally {
                isLoading = false
            }
        }
    }

    // Filter state management methods - in-memory only
    override fun saveFilters(filterState: FilterState) {
        _filtersFlow.value = filterState
    }

    override suspend fun getEventById(eventId: String): Resource<CardItem> {
        return withContext(Dispatchers.IO) {
            try {
                KmpLog.d("EventRepository", "Загрузка события по ID: $eventId")

                val result = postman.get<CardItem>(
                    baseUrl = urlWorker.baseUrl,
                    route = urlWorker.getEventByIdRoute(eventId),
                    headers = mapOf(
                        "Authorization" to "Bearer ${urlWorker.getAuthToken()}"
                    )
                )
                result
            } catch (e: Exception) {
                KmpLog.e(
                    "EventRepository",
                    "Исключение при загрузке события по ID $eventId: ${e.message}"
                )
                Resource.Error("Ошибка загрузки события: ${e.message}")
            }
        }
    }

    override fun clearFilters() {
        _filtersFlow.update { FilterState() }
    }

    override fun getCurrentFilters(): FilterState {
        return _filtersFlow.value
    }
}