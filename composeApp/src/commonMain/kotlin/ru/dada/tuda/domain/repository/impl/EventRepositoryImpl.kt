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

class EventRepositoryImpl(
    private val postman: Postman,
    private val urlWorker: UrlWorker
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
        if (current == null || current is Resource.Empty || current is Resource.Error) {
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
                if (result.data?.isEmpty() == false)
                    _cardsLiveData.update { result }
                else
                    loadMockMoreCards()
            } catch (e: Exception) {
                _cardsLiveData.update { Resource.Error("Ошибка при загрузке событий: ${e.message}") }
                KmpLog.e("EventRepository", "Ошибка при загрузке событий: ${e.message}")
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
                    if (currentCards.isEmpty())
                        _cardsLiveData.update { Resource.Success(currentCards) }
                    else
                        loadMockMoreCards()
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
                    // For mock data, simulate pagination end
                    loadMockMoreCards()
                }
            } catch (e: Exception) {
                KmpLog.e(
                    "EventRepository",
                    "Ошибка при загрузке дополнительных событий: ${e.message}"
                )
                // For mock data, simulate pagination end
                loadMockMoreCards()
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadMockMoreCards() {
        val currentCards = _cardsLiveData.value.data?.toMutableList() ?: mutableListOf()
//        if (currentCards.size >= MAX_CARDS) {
//            hasMorePages = false
//            return
//        }

        // Add a few more mock cards
        val newMockCard = CardItem(
            id = "mock_${currentCards.size + 1}",
            imageURL = listOf("https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?w=400&h=600&fit=crop&crop=center"),
            title = "Новое событие ${currentCards.size + 1}",
            description = "Описание дополнительного события для тестирования пагинации.",
            city = "Москва",
            address = "ул. Тестовая, ${currentCards.size + 1}",
            locationName = "Тестовая площадка",
            price = "${(currentCards.size + 1) * 100}",
            priceType = "руб",
            type = "test",
            tags = mutableListOf("тест", "пагинация"),
            categories = mutableListOf("Тестирование"),
            date = "2024-03-01T${10 + currentCards.size}:00:00",
            dateEnd = "2024-03-01T${12 + currentCards.size}:00:00",
            referralLink = "https://example.com/tickets/mock_${currentCards.size + 1}",
            source = "Mock",
            creatorId = "mock_creator",
            views = currentCards.size * 10,
            likes = currentCards.size,
            isFavorite = false
        )

        currentCards.add(newMockCard)
        _cardsLiveData.update { Resource.Success(currentCards) }
        currentPage++
        hasMorePages = currentCards.size < MAX_CARDS

        KmpLog.d(
            "EventRepository",
            "Добавлена моковая карточка. Всего: ${currentCards.size}, hasMore: $hasMorePages"
        )
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