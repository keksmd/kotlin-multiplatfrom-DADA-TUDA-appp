package ru.dada.tuda.domain.util

// Мультиплатформенный UrlWorker без Android зависимостей.
// Содержит базовые домены и пути. При необходимости можно вынести в конфиг.
class UrlWorker(private val tokenManager: TokenManager) {
    companion object {
        // Домены
        const val baseUrl: String = "https://api.dada-tuda.ru"
        const val authUrl: String = "https://auth.dada-tuda.ru"
        const val apiVersion: String = "v1"

        // Пути (части)
        private const val EVENTS = "/api/v3/events/for"
        private const val CATEGORIES = "/api/v1/events/categories"
        // Было EVENT_BY_ID_TEMPLATE = "/api/v1/events/%s" -> используем префикс
        private const val EVENT_BY_ID_PREFIX = "/api/v1/events/"
        // Было FEEDBACK_VERSIONED_TEMPLATE = "/api/%s/feedback" -> собираем вручную
        private const val FEEDBACK_AUTH = "/api/v3/feedback"
        private const val SHORTLIST = "/api/v4/shortlist"
        private const val LOGIN_TOKEN = "/realms/master/protocol/openid-connect/token"
        private const val REGISTER = "/api/v2/users/register"
    }

    // Instance properties for backward compatibility (old code used urlWorker.baseUrl / authUrl)
    val baseUrl: String get() = Companion.baseUrl
    val authUrl: String get() = Companion.authUrl

    // Полные URL
    fun getEventsUrl(): String = baseUrl + EVENTS
    fun getCategoriesUrl(): String = baseUrl + CATEGORIES
    fun getEventByIdUrl(eventId: String): String = baseUrl + EVENT_BY_ID_PREFIX + eventId
    fun getFeedbackUrl(): String = "$baseUrl/api/$apiVersion/feedback"
    fun getFeedbackAuthUrl(): String = baseUrl + FEEDBACK_AUTH
    fun getShortlistUrl(): String = baseUrl + SHORTLIST
    fun getAddToShortlistUrl(): String = getShortlistUrl()
    fun getRemoveFromShortlistUrl(eventId: String): String = getShortlistUrl() + "/" + eventId
    fun getRegisterUrl(): String = baseUrl + REGISTER
    fun getLoginTokenUrl(): String = authUrl + LOGIN_TOKEN

    // "Route" части без домена (для Postman / конструирования вручную)
    fun registerRoute(): String = REGISTER
    fun loginTokenRoute(): String = LOGIN_TOKEN
    fun eventsRoute(): String = EVENTS
    fun categoriesRoute(): String = CATEGORIES
    fun shortlistRoute(): String = SHORTLIST
    fun feedbackAuthRoute(): String = FEEDBACK_AUTH

    // Совместимость со старыми именами методов (для старого кода):
    fun getEventsRoute(): String = eventsRoute()
    fun getCategoriesRoute(): String = categoriesRoute()
    fun getShortListRoute(): String = shortlistRoute()
    fun getFeedbackRoute(): String = feedbackAuthRoute()
    fun getEventByIdRoute(eventId: String): String = EVENT_BY_ID_PREFIX + eventId
    fun createNewUserRoute(): String = registerRoute()
    fun createNewUser(): String = registerRoute()
    fun getTokenForLoginRoute(): String = loginTokenRoute()
    fun getFeedbackRouteUrl(): String = getFeedbackUrl()

    // Токен авторизации (делегируем TokenManager)
    fun getAuthToken(): String = tokenManager.getToken() ?: ""
}