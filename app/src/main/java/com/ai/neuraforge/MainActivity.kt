package com.ai.neuraforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import com.ai.neuraforge.ui.theme.PdfAiTheme
import com.ai.neuraforge.viewmodel.ChatViewModel
import com.ai.neuraforge.repository.ChatRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ai.neuraforge.ui.screens.ChatScreen

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = ChatRepository(applicationContext.contentResolver)

        setContent {
            PdfAiTheme {
                val factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ChatViewModel(repo) as T
                    }
                }
                val vm: ChatViewModel = viewModel(factory = factory)
                ChatScreen(viewModel = vm)
            }
        }
    }
}
