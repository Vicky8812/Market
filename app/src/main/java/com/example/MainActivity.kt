package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.local.MarketDatabase
import com.example.data.remote.GeminiService
import com.example.data.repository.MarketRepository
import com.example.ui.MarketPulseApp
import com.example.ui.theme.MarketPulseTheme

class MainActivity : ComponentActivity() {

    private lateinit var repository: MarketRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = MarketDatabase.getInstance(applicationContext)
        val geminiService = GeminiService()
        repository = MarketRepository(
            portfolioDao = db.portfolioDao(),
            watchlistDao = db.watchlistDao(),
            alertDao = db.alertDao(),
            geminiService = geminiService
        )

        setContent {
            MarketPulseTheme(darkTheme = true) {
                MarketPulseApp(repository = repository)
            }
        }
    }
}
