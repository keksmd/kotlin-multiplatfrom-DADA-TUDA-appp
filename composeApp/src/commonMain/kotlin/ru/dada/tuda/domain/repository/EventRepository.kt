package ru.dada.tuda.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.dada.tuda.domain.util.Resource
import ru.dada.tuda.domain.http.models.CardItem
import ru.dada.tuda.domain.http.models.filters.EventFilterParams
import ru.dada.tuda.domain.models.FilterState

interface EventRepository {
    val cardsLiveData: StateFlow<Resource<MutableList<CardItem>>>
    val filtersFlow: StateFlow<FilterState>

    suspend fun initializeCardsIfEmpty()
    suspend fun removeCardAndLoadOne(position: Int)
    suspend fun loadExactNumberOfCards(count: Int)
    suspend fun reloadWithFilters(params: EventFilterParams)
    suspend fun loadMoreCards()
    suspend fun getEventById(eventId: String): Resource<CardItem>
    fun addCard(card: CardItem?)
    
    // Filter persistence methods
    fun saveFilters(filterState: FilterState)
    fun clearFilters()
    fun getCurrentFilters(): FilterState
}