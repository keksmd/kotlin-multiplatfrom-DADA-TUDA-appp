package ru.dada.tuda

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import ru.dada.tuda.di.appModule
import ru.dada.tuda.di.platformModule
import ru.dada.tuda.presentation.compose.navigation.AppNavigation
import ru.dada.tuda.presentation.theme.MyApplicationTheme

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(platformModule, appModule)
    }) {
        MyApplicationTheme {
            AppContent()
        }
    }
}

@Composable
        modifier = Modifier.fillMaxSize().platformSystemBars(),
    val navController = rememberNavController()

    Surface(
            modifier = Modifier.fillMaxSize()
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)

        ) { innerPadding ->
            AppNavigation(Modifier.padding(innerPadding), navController)
        }
    }
}