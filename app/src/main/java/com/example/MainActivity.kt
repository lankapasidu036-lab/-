package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.data.db.GameDatabase
import com.example.data.repository.GameRepository
import com.example.ui.GameViewModel
import com.example.ui.components.CrashGameScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    // Lazy initialization of Room database on local worker
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            GameDatabase::class.java,
            "kapila_crash_game.db"
        )
        .fallbackToDestructiveMigration() // safe migration strategy for development
        .build()
    }

    private val repository by lazy {
        GameRepository(database.gameDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Inline ViewModel Factory injection
                val factory = rememberViewModelFactory {
                    GameViewModel(application, repository)
                }

                val gameViewModel: GameViewModel = viewModel(factory = factory)
                
                // Observe reactive database flows at main content layer
                val pastMultipliers by repository.multiplierHistory.collectAsState(initial = emptyList())
                val myBetsHistory by repository.betHistory.collectAsState(initial = emptyList())

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CrashGameScreen(
                        viewModel = gameViewModel,
                        pastRounds = pastMultipliers,
                        myBets = myBetsHistory,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Compact, clean local helper to instantiate ViewModels with direct constructor parameters
inline fun <reified T : ViewModel> rememberViewModelFactory(
    crossinline creator: () -> T
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
            @Suppress("UNCHECKED_CAST")
            return creator() as VM
        }
    }
}
