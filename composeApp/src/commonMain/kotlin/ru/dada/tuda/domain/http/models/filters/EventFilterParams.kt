package ru.dada.tuda.domain.http.models.filters

data class EventFilterParams(
    val categories: List<String> = emptyList(),
    val startDateTime: String? = null,
    val endDateTime: String? = null,
    val minPrice: String? = null,
    val maxPrice: String? = null,
    val search: String = ""
) {
    fun getParams(): Map<String, String> {
        val params = mutableMapOf<String, String>()
        if (categories.isNotEmpty()) params["categories"] = categories.joinToString(",")
        startDateTime?.let { params["startDateTime"] = it }
        endDateTime?.let { params["endDateTime"] = it }
        minPrice?.let { params["minPrice"] = it }
        maxPrice?.let { params["maxPrice"] = it }
        search.let { if (it.isNotEmpty()) params["search"] = it }
        return params
    }
}
