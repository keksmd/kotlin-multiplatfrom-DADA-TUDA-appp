package ru.dada.tuda.presentation.compose.screens.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.dada.tuda.domain.repository.EventRepository
import ru.dada.tuda.domain.models.FilterState
import ru.dada.tuda.domain.http.models.filters.EventFilterParams

class FilterEventsViewModel(
    private val eventRepository: EventRepository
): ViewModel() {
    private val _categoriesFlow = MutableStateFlow<List<String>>(emptyList())
    val categoriesFlow: StateFlow<List<String>> = _categoriesFlow.asStateFlow()
    private val _startDateTimeFlow = MutableStateFlow<String?>(null)
    val startDateTimeFlow: StateFlow<String?> = _startDateTimeFlow.asStateFlow()
    private val _endDateTimeFlow = MutableStateFlow<String?>(null)
    val endDateTimeFlow: StateFlow<String?> = _endDateTimeFlow.asStateFlow()
    private val _minPriceFlow = MutableStateFlow(0)
    val minPriceFlow: StateFlow<Int> = _minPriceFlow.asStateFlow()
    private val _maxPriceFlow = MutableStateFlow(10_000)
    val maxPriceFlow: StateFlow<Int> = _maxPriceFlow.asStateFlow()
    private val _searchFlow = MutableStateFlow("")
    val searchFlow: StateFlow<String> = _searchFlow.asStateFlow()
    private val _isStarredFlow = MutableStateFlow(false)
    val isStarredFlow: StateFlow<Boolean> = _isStarredFlow.asStateFlow()
    private val _updateFlow = MutableStateFlow(true)

    // Публичный поток единого состояния фильтра (проксируем репозиторий)
    val filterStateFlow: StateFlow<FilterState> get() = eventRepository.filtersFlow

    // Инициализация локальных стейтов значениями из репозитория и подписка на изменения
    init {
        applyFilterState(eventRepository.getCurrentFilters())
//        viewModelScope.launch {
//            eventRepository.filtersFlow.collect { applyFilterState(it) }
//        }
    }

    private fun applyFilterState(state: FilterState) {
        _categoriesFlow.value = state.categories
        _startDateTimeFlow.value = state.startDateTime
        _endDateTimeFlow.value = state.endDateTime
        _minPriceFlow.value = state.minPrice?.toIntOrNull() ?: 0
        _maxPriceFlow.value = state.maxPrice?.toIntOrNull() ?: 10_000
        _searchFlow.value = state.search
        _isStarredFlow.value = state.isStarred
    }

    fun applyFilters() {
        // Сохраняем текущее состояние фильтров в репозитории
        saveCurrentFilters()
        // Запускаем перезагрузку событий с учетом применённых фильтров
        viewModelScope.launch {
            val params = EventFilterParams(
                categories = _categoriesFlow.value,
                startDateTime = _startDateTimeFlow.value,
                endDateTime = _endDateTimeFlow.value,
                minPrice = _minPriceFlow.value.takeIf { it > 0 }?.toString(),
                maxPrice = _maxPriceFlow.value.takeIf { it < 10_000 }?.toString(),
                search = _searchFlow.value
            )
            eventRepository.reloadWithFilters(params)
        }
        _updateFlow.update { true }
    }

    private fun saveCurrentFilters() {
        val currentState = FilterState(
            categories = _categoriesFlow.value,
            startDateTime = _startDateTimeFlow.value,
            endDateTime = _endDateTimeFlow.value,
            minPrice = _minPriceFlow.value.takeIf { it > 0 }?.toString(),
            maxPrice = _maxPriceFlow.value.takeIf { it < 10_000 }?.toString(),
            search = _searchFlow.value,
            isStarred = _isStarredFlow.value
        )
        eventRepository.saveFilters(currentState)
    }

    fun updateStarred() {
        _isStarredFlow.update { !it }
        saveCurrentFilters()
    }

    fun updateSearch(search: String) {
        _searchFlow.update { search }
        saveCurrentFilters()
    }

    // Методы обновления фильтров для экрана фильтров
    fun updateCategories(categories: List<String>) {
        _categoriesFlow.value = categories
    }

    fun updateStartDateTime(value: String?) {
        _startDateTimeFlow.value = value
    }

    fun updateEndDateTime(value: String?) {
        _endDateTimeFlow.value = value
    }

    fun updateMinPrice(value: Int) {
        _minPriceFlow.value = value
    }

    fun updateMaxPrice(value: Int) {
        _maxPriceFlow.value = value
    }

    fun clearFilters() {
        _categoriesFlow.value = emptyList()
        _startDateTimeFlow.value = null
        _endDateTimeFlow.value = null
        _minPriceFlow.value = 0
        _maxPriceFlow.value = 10_000
        // Поиск и звездочки не трогаем при сбросе обычных фильтров
        saveCurrentFilters()
    }

    fun clearAllFilters() {
        // Сбрасываем и локальное состояние, и репозиторий, чтобы не восстановить старые значения при следующих апдейтах
        _categoriesFlow.value = emptyList()
        _startDateTimeFlow.value = null
        _endDateTimeFlow.value = null
        _minPriceFlow.value = 0
        _maxPriceFlow.value = 10_000
        _searchFlow.value = ""
        _isStarredFlow.value = false
        eventRepository.clearFilters()
    }
}