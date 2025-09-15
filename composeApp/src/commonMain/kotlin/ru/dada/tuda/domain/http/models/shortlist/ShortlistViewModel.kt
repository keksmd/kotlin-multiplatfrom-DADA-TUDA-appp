package ru.dada.tuda.domain.http.models.shortlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.dada.tuda.domain.http.models.filters.FeedbackFilterParams
import ru.dada.tuda.domain.repository.ShortlistRepository

class ShortlistViewModel(
    private val shortlistRepository: ShortlistRepository
) : ViewModel() {

    // Используем только потоковый подход - напрямую из репозитория
    val categoriesFlow = shortlistRepository.filtersFlow
        .map { it.categories }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<String>())

    val startDateTimeFlow = shortlistRepository.filtersFlow
        .map { it.startDateTime }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null as String?)

    val endDateTimeFlow = shortlistRepository.filtersFlow
        .map { it.endDateTime }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null as String?)

    val minPriceFlow = shortlistRepository.filtersFlow
        .map { it.minPrice }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null as String?)

    val maxPriceFlow = shortlistRepository.filtersFlow
        .map { it.maxPrice }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null as String?)

    val searchFlow = shortlistRepository.filtersFlow
        .map { it.search }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val isStarredFlow = shortlistRepository.filtersFlow
        .map { it.isStarred }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Проверка наличия активных фильтров
    val hasActiveFiltersFlow = shortlistRepository.filtersFlow
        .map { filters ->
            filters.categories.isNotEmpty() ||
            !filters.startDateTime.isNullOrEmpty() ||
            !filters.endDateTime.isNullOrEmpty() ||
            !filters.minPrice.isNullOrEmpty() ||
            !filters.maxPrice.isNullOrEmpty() ||
            filters.search.isNotEmpty() ||
            filters.isStarred
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _updateFlow = MutableStateFlow(true)
    private val _favoriteOverrides = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val favoriteOverrides: StateFlow<Map<String, Boolean>> = _favoriteOverrides.asStateFlow()

    @OptIn(FlowPreview::class)
    val filterParams = combine(
        combine(
            categoriesFlow.debounce(300),
            startDateTimeFlow.debounce(300),
            endDateTimeFlow.debounce(300),
            minPriceFlow.debounce(300)
        ) { categories, startDate, endDate, minPrice ->
            FilterParamsPartial1(categories, startDate, endDate, minPrice)
        },
        combine(
            maxPriceFlow.debounce(300),
            searchFlow.debounce(500),
            isStarredFlow,
            _updateFlow
        ) { maxPrice, search, isStarred, update ->
            FilterParamsPartial2(maxPrice, search, isStarred, update)
        }
    ) { partial1, partial2 ->
        if (!partial2.update) return@combine null

        FeedbackFilterParams(
            categories = partial1.categories,
            startDateTime = partial1.startDate,
            endDateTime = partial1.endDate,
            minPrice = partial1.minPrice,
            maxPrice = partial2.maxPrice,
            search = partial2.search,
            isStarred = partial2.isStarred
        )
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FeedbackFilterParams())

    // Вспомогательные data classes для разбиения combine
    private data class FilterParamsPartial1(
        val categories: List<String>,
        val startDate: String?,
        val endDate: String?,
        val minPrice: String?
    )

    private data class FilterParamsPartial2(
        val maxPrice: String?,
        val search: String,
        val isStarred: Boolean,
        val update: Boolean
    )

    init {
        viewModelScope.launch {
            filterParams.collectLatest { params ->
                if (params != null) {
                    // Очистим локальные оверрайды при полном обновлении фильтров/поиска
                    _favoriteOverrides.value = emptyMap()
                    getShortlist(params)
                }
            }
        }
    }

    val shortlistLiveData = shortlistRepository.shortlistLiveData
    val paginationState = shortlistRepository.paginationState

    fun getShortlist(params: FeedbackFilterParams? = filterParams.value) {
        viewModelScope.launch {
            if (params != null)
                shortlistRepository.loadShortlist(params, refresh = true)
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            shortlistRepository.loadNextPage()
        }
    }

    fun applyFilters() {
        _updateFlow.update { true }
    }

    fun updateStarred() {
        shortlistRepository.updateFilters { it.copy(isStarred = !it.isStarred) }
    }

    fun updateSearch(search: String) {
        shortlistRepository.updateFilters { it.copy(search = search) }
    }

    fun toggleFavorite(id: String, current: Boolean) {
        _favoriteOverrides.update { it + (id to !current) }
    }

    // Методы обновления фильтров для экрана фильтров - теперь работают через репозиторий
    fun updateCategories(categories: List<String>) {
        shortlistRepository.updateFilters { it.copy(categories = categories) }
    }

    fun updateStartDateTime(value: String?) {
        shortlistRepository.updateFilters { it.copy(startDateTime = value) }
    }

    fun updateEndDateTime(value: String?) {
        shortlistRepository.updateFilters { it.copy(endDateTime = value) }
    }

    fun updateMinPrice(value: String?) {
        shortlistRepository.updateFilters { it.copy(minPrice = value) }
    }

    fun updateMaxPrice(value: String?) {
        shortlistRepository.updateFilters { it.copy(maxPrice = value) }
    }

    fun clearFilters() {
        shortlistRepository.updateFilters {
            it.copy(
                categories = emptyList(),
                startDateTime = null,
                endDateTime = null,
                minPrice = null,
                maxPrice = null,
                isStarred = false,
                search = ""
            )
        }
    }

    fun clearAllFilters() {
        shortlistRepository.clearFilters()
    }
}
