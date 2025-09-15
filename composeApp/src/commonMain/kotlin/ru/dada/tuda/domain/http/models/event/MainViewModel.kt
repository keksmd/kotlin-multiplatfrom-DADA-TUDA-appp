package ru.dada.tuda.domain.http.models.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ru.dada.tuda.domain.http.models.filters.EventFilterParams
import ru.dada.tuda.domain.repository.EventRepository

class MainViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {
    val cardsLiveData = eventRepository.cardsLiveData

    // Фильтры
    private val _categoriesFlow = MutableStateFlow<List<String>>(emptyList())
    val categoriesFlow = _categoriesFlow.asStateFlow()
    private val _startDateTimeFlow = MutableStateFlow<String?>(null)
    val startDateTimeFlow = _startDateTimeFlow.asStateFlow()
    private val _endDateTimeFlow = MutableStateFlow<String?>(null)
    val endDateTimeFlow = _endDateTimeFlow.asStateFlow()
    private val _minPriceFlow = MutableStateFlow<String?>(null)
    val minPriceFlow = _minPriceFlow.asStateFlow()
    private val _maxPriceFlow = MutableStateFlow<String?>(null)
    val maxPriceFlow = _maxPriceFlow.asStateFlow()
    private val _searchFlow = MutableStateFlow("")
    val searchFlow = _searchFlow.asStateFlow()
    private val _updateFlow = MutableStateFlow(true)

    @OptIn(FlowPreview::class)
    val filterParams = combine(
        _categoriesFlow.debounce(300),
        _startDateTimeFlow.debounce(300),
        _endDateTimeFlow.debounce(300),
        _minPriceFlow.debounce(300),
        _maxPriceFlow.debounce(300),
        _searchFlow.debounce(500),
        _updateFlow
    ) { values ->
        val shouldUpdate = values[6] as Boolean
        if (!shouldUpdate) return@combine null
        val categories = values[0] as List<String>
        val start = values[1] as String?
        val end = values[2] as String?
        val min = values[3] as String?
        val max = values[4] as String?
        val search = values[5] as String
        EventFilterParams(
            categories = categories,
            startDateTime = start,
            endDateTime = end,
            minPrice = min,
            maxPrice = max,
            search = search
        )
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventFilterParams())

    init {
        viewModelScope.launch {
            filterParams.collectLatest { params ->
                if (params != null) {
                    eventRepository.reloadWithFilters(params)
                }
            }
        }
    }

    fun initializeCardsIfEmpty() {
        viewModelScope.launch {
            eventRepository.initializeCardsIfEmpty()
        }
    }

    fun removeCardAndLoadOne(position: Int) {
        viewModelScope.launch {
            eventRepository.removeCardAndLoadOne(position)
        }
    }

    fun loadExactNumberOfCards(count: Int) {
        viewModelScope.launch {
            eventRepository.loadExactNumberOfCards(count)
        }
    }

    // Методы обновления фильтров
    fun applyFilters() { _updateFlow.update { true } }
    fun updateCategories(value: List<String>) { _categoriesFlow.value = value }
    fun updateStartDateTime(value: String?) { _startDateTimeFlow.value = value }
    fun updateEndDateTime(value: String?) { _endDateTimeFlow.value = value }
    fun updateMinPrice(value: String?) { _minPriceFlow.value = value }
    fun updateMaxPrice(value: String?) { _maxPriceFlow.value = value }
    fun updateSearch(value: String) { _searchFlow.value = value }
    fun clearFilters() {
        _categoriesFlow.value = emptyList()
        _startDateTimeFlow.value = null
        _endDateTimeFlow.value = null
        _minPriceFlow.value = null
        _maxPriceFlow.value = null
        // Поиск не сбрасываем
    }
}
