package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.WikipediaService
import com.example.data.database.FootballDatabase
import com.example.data.repository.FootballRepository
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FootballViewModel
import com.example.viewmodel.FootballViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize local database and services
        val database = FootballDatabase.getDatabase(applicationContext)
        val wikiService = WikipediaService.create()
        val repository = FootballRepository(
            teamDao = database.teamDao(),
            playerDao = database.playerDao(),
            matchDao = database.matchDao(),
            userDao = database.userDao(),
            postDao = database.postDao(),
            commentDao = database.commentDao(),
            wikiService = wikiService
        )

        // Instantiate FootballViewModel
        val viewModel: FootballViewModel by viewModels {
            FootballViewModelFactory(application, repository)
        }

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
