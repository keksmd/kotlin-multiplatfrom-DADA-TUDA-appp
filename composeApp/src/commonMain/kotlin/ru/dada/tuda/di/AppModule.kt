package ru.dada.tuda.di

import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.dsl.module
import ru.dada.tuda.Greeting
import ru.dada.tuda.domain.http.models.auth.AuthViewModel
import ru.dada.tuda.domain.http.models.event.MainViewModel
import ru.dada.tuda.domain.http.models.feedback.FeedbackViewModel
import ru.dada.tuda.domain.http.models.shortlist.ShortlistViewModel
import ru.dada.tuda.domain.http.models.singleevent.SingleEventViewModel
import ru.dada.tuda.domain.repository.AuthRepository
import ru.dada.tuda.domain.repository.EventRepository
import ru.dada.tuda.domain.repository.FeedbackRepository
import ru.dada.tuda.domain.repository.ShortlistRepository
import ru.dada.tuda.domain.repository.impl.AuthRepositoryImpl
import ru.dada.tuda.domain.repository.impl.EventRepositoryImpl
import ru.dada.tuda.domain.repository.impl.FeedbackRepositoryImpl
import ru.dada.tuda.domain.repository.impl.ShortlistRepositoryImpl
import ru.dada.tuda.domain.usecase.SubmitFeedbackUseCase
import ru.dada.tuda.domain.util.Postman
import ru.dada.tuda.domain.util.UrlWorker

val appModule = module {
    // Basic services
    single { Greeting() }
    single { Postman() }
    single { UrlWorker(get()) }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<EventRepository> { EventRepositoryImpl(get(), get()) }
    single<FeedbackRepository> { FeedbackRepositoryImpl(get(), get()) }
    single<ShortlistRepository> { ShortlistRepositoryImpl(get(), get()) }

    single { SubmitFeedbackUseCase(get(), get()) }

    // ViewModels
    factory { AuthViewModel(get(), get()) }
    factory { MainViewModel(get()) }
    factory { FeedbackViewModel(get()) }
    factory { ShortlistViewModel(get()) }
    factory { SingleEventViewModel(get(), get()) }

    // Add more common (platform-independent) dependencies here
}