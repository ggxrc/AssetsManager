package com.ads.assetsmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.ads.assetsmanager.data.database.GameDatabase
import com.ads.assetsmanager.data.repository.GameRepository
import com.ads.assetsmanager.ui.screens.GameNavHost
import com.ads.assetsmanager.ui.theme.AssetsManagerTheme
import com.ads.assetsmanager.viewmodel.GameViewModel
import com.ads.assetsmanager.viewmodel.GameViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicialização Manual (Sem Hilt)
        val database = GameDatabase.getDatabase(this)
        val repository = GameRepository(database.gameDao())
        val factory = GameViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[GameViewModel::class.java]

        setContent {
            AssetsManagerTheme { // Seu tema padrão
                GameNavHost(viewModel)
            }
        }
    }
}