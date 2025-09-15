package ru.dada.tuda.domain.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ru.dada.tuda.domain.http.models.filters.FeedbackFilterParams
import ru.dada.tuda.domain.http.models.shortlist.ShortListDTO
import ru.dada.tuda.domain.http.models.shortlist.ShortlistEventDTO
import ru.dada.tuda.domain.repository.PaginationState
import ru.dada.tuda.domain.repository.ShortlistRepository
import ru.dada.tuda.domain.util.KmpLog
import ru.dada.tuda.domain.util.Postman
import ru.dada.tuda.domain.util.Resource
import ru.dada.tuda.domain.util.UrlWorker
import ru.dada.tuda.domain.models.FilterState
import ru.dada.tuda.domain.http.models.CardItem

class ShortlistRepositoryImpl(
    private val postman: Postman,
    private val urlWorker: UrlWorker
) : ShortlistRepository {

    private val _shortlistLiveData = MutableStateFlow<Resource<ShortListDTO>>(Resource.Loading())
    override val shortlistLiveData: StateFlow<Resource<ShortListDTO>> = _shortlistLiveData.asStateFlow()

    private val _paginationState = MutableStateFlow(PaginationState())
    override val paginationState: StateFlow<PaginationState> = _paginationState.asStateFlow()

    // Filter state management - in-memory only
    private val _filtersFlow = MutableStateFlow(FilterState())
    override val filtersFlow: StateFlow<FilterState> = _filtersFlow.asStateFlow()

    private var currentFilters: FeedbackFilterParams? = null
    private var allItems = mutableListOf<ShortlistEventDTO>()
    private val pageSize = 10

    override suspend fun loadShortlist(feedbackFilterParams: FeedbackFilterParams, refresh: Boolean) {
        if (refresh)
            _shortlistLiveData.value = Resource.Loading()

        currentFilters = feedbackFilterParams
        
        withContext(Dispatchers.IO) {
            try {
                val targetPage = if (refresh) 1 else _paginationState.value.currentPage + 1

                // Устанавливаем состояние загрузки
                _paginationState.value = PaginationState(
                    isLoading = refresh,
                    isLoadingMore = !refresh,
                    currentPage = targetPage,
                    hasMorePages = true,
                    error = null
                )

                KmpLog.d("ShortlistRepository", "Загружаем страницу: $targetPage, pageSize: $pageSize")

                val result = postman.get<ShortListDTO>(
                    baseUrl = UrlWorker.baseUrl,
                    route = urlWorker.shortlistRoute(),
                    arguments = mapOf(
                        "page_size" to pageSize,
                        "page_number" to targetPage
                    ) + feedbackFilterParams.getParams(),
                    headers = mapOf(
                        "Authorization" to "Bearer ${urlWorker.getAuthToken()}"
                    )
                )

                when (result) {
                    is Resource.Success -> {
                        val newItems = result.data.content.orEmpty()
                        val totalPages = result.data.totalPages
                        val hasMore = targetPage < totalPages

                        KmpLog.d("ShortlistRepository", "Получено элементов: ${newItems.size}, текущая страница: $targetPage, всего страниц: $totalPages, refresh: $refresh")

                        if (refresh) {
                            allItems.clear()
                        }
                        allItems.addAll(newItems)

                        KmpLog.d("ShortlistRepository", "Добавлено элементов: ${newItems.size}, всего в списке: ${allItems.size}")

                        _paginationState.value = PaginationState(
                            isLoading = false,
                            isLoadingMore = false,
                            hasMorePages = hasMore,
                            currentPage = targetPage,
                            error = null
                        )

                        // Создаем новый DTO с объединенными данными
                        val combinedResult = result.copy(data = result.data.copy(content = allItems))
                        println("combinedResult: $combinedResult")
                        _shortlistLiveData.value = combinedResult
                    }
                    is Resource.Empty -> {
                        if (refresh) {
                            allItems.clear()
                        }
                        _paginationState.value = _paginationState.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            hasMorePages = false,
                            error = null
                        )
                        _shortlistLiveData.value = result
                    }
                    is Resource.Error -> {
                        _paginationState.value = _paginationState.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            hasMorePages = false,
                            error = result.message
                        )
                        _shortlistLiveData.value = result
                    }
                    is Resource.Loading -> { /* ignore */ }
                }
            } catch (e: Exception) {
                KmpLog.e("ShortlistRepository", "Ошибка при загрузке shortlist: ${e.message}")
                _paginationState.value = _paginationState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    hasMorePages = false,
                    error = e.message
                )
                _shortlistLiveData.value = Resource.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    override suspend fun loadNextPage() {
        val currentState = _paginationState.value
        if (currentState.isLoading || currentState.isLoadingMore || !currentState.hasMorePages) {
            return
        }
        
        currentFilters?.let { filters ->
            loadShortlist(filters, refresh = false)
        }
    }

    // Filter state management methods - in-memory only
    override fun saveFilters(filterState: FilterState) {
        _filtersFlow.value = filterState
    }

    override fun updateFilters(update: (FilterState) -> FilterState) {
        _filtersFlow.value = update(_filtersFlow.value)
    }

    override fun clearFilters() {
        _filtersFlow.value = FilterState()
    }

    override fun getCurrentFilters(): FilterState {
        return _filtersFlow.value
    }
    
    // Individual item management methods
    override fun getCurrentShortlistItems(): List<CardItem> {
        return allItems.map { CardItem(it) }
    }
    
    override fun updateItemFavoriteStatus(itemId: String, isFavorite: Boolean) {
        val itemIndex = allItems.indexOfFirst { it.id == itemId }
        if (itemIndex >= 0) {
            val updatedItem = allItems[itemIndex].copy(isFavorite = isFavorite)
            allItems[itemIndex] = updatedItem
            
            val currentData = _shortlistLiveData.value
            if (currentData is Resource.Success) {
                val updatedDTO = currentData.data.copy(content = allItems)
                _shortlistLiveData.value = Resource.Success(updatedDTO)
            }
        }
    }
    
    override fun removeItem(itemId: String) {
        val itemIndex = allItems.indexOfFirst { it.id == itemId }
        if (itemIndex >= 0) {
            allItems.removeAt(itemIndex)
            
            val currentData = _shortlistLiveData.value
            if (currentData is Resource.Success) {
                val updatedDTO = currentData.data.copy(content = allItems)
                _shortlistLiveData.value = Resource.Success(updatedDTO)
            }
        }
    }
    
    override fun findItemById(itemId: String): CardItem? {
        val item = allItems.find { it.id == itemId }
        return item?.let { CardItem(it) }
    }
}
