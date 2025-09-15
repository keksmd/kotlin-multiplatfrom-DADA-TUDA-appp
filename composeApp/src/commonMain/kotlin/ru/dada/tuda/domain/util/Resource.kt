package ru.dada.tuda.domain.util

/** Resource is a sealed class for states loading from server **/
sealed class Resource<T>(
    open val data: T? = null,
    open val message: String? = null,
    open val status: Int = 0
) {
    /** Success is Resource, which is success **/
    data class Success<T>(override var data: T, override val status: Int = 200) : Resource<T>(data)

    /** Loading is Resource, which is loading process **/
    data class Loading<T>(override var data: T? = null) : Resource<T>(data)

    /** Пустой успешный ответ (например, HTTP 204 No Content) **/
    data class Empty<T>(override val status: Int = 204) : Resource<T>(null, null, status)

    /** Error is Resource, which is error **/
    data class Error<T>(
        override val message: String,
        override val status: Int = 404,
        override var data: T? = null
    ) : Resource<T>(data, message)

    fun onSuccess(function: (Success<T>) -> Unit): Resource<T> {
        if (this is Success) function(this)
        return this
    }

    fun onLoading(function: (Loading<T>) -> Unit): Resource<T> {
        if (this is Loading) function(this)
        return this
    }

    fun onEmpty(function: (Empty<T>) -> Unit): Resource<T> {
        if (this is Empty) function(this)
        return this
    }

    fun onError(function: (Error<T>) -> Unit): Resource<T> {
        if (this is Error) function(this)
        return this
    }

    /** Static fields **/
    enum class ErrorsRequest(val desc: String) {
        ERROR_SERIALIZATION("Error with serialization data"),
        ERROR_USER_CREDENTIALS("Error with login and password user"),
        ERROR_UNKNOWN("Unknown error"),
        ERROR_TIMEOUT("Timeout connect"),
        ERROR_NOT_VALID_TOKEN("Not valid token"),
        ERROR_NOT_FOUND("Not found")
    }
}
