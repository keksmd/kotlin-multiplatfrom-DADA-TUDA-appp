package ru.dada.tuda.presentation.compose.navigation

enum class Screen(val route: String) {
    MAIN("main"),
    AUTH("auth"),
    REGISTRATION("registration"),
    PROFILE("profile"),
    EVENTS("events"),
    FEEDBACK("feedback"),
    SHORTLIST("shortlist"),
    FILTERS("filters"),
    // Экран фильтров для событий
    FILTERS_EVENTS("filters_events"),
    // Экран просмотра одного события
    SINGLE_EVENT("single_event");

    companion object {
        fun singleEvent(eventId: String) = "single_event/$eventId"
    }
}