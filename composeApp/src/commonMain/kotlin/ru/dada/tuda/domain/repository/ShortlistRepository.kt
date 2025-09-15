package ru.dada.tuda.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.dada.tuda.domain.http.models.filters.FeedbackFilterParams
import ru.dada.tuda.domain.http.models.shortlist.ShortListDTO
import ru.dada.tuda.domain.util.Resource
import ru.dada.tuda.domain.models.FilterState
import ru.dada.tuda.domain.http.models.CardItem

interface ShortlistRepository {
    val shortlistLiveData: StateFlow<Resource<ShortListDTO>>
    val paginationState: StateFlow<PaginationState>
    val filtersFlow: StateFlow<FilterState>

    suspend fun loadShortlist(feedbackFilterParams: FeedbackFilterParams, refresh: Boolean = true)
    suspend fun loadNextPage()
    
    // Filter persistence methods
    fun saveFilters(filterState: FilterState)
    fun updateFilters(update: (FilterState) -> FilterState)
    fun clearFilters()
    fun getCurrentFilters(): FilterState
    
    // Individual item management methods
    fun getCurrentShortlistItems(): List<CardItem>
    fun updateItemFavoriteStatus(itemId: String, isFavorite: Boolean)
    fun removeItem(itemId: String)
    fun findItemById(itemId: String): CardItem?
}

data class PaginationState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = true,
    val currentPage: Int = 1,
    val error: String? = null
)
