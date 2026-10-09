package com.sriramanappindi.openwire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sriramanappindi.openwire.ui.MainScreen
import com.sriramanappindi.openwire.ui.NewsViewModel
import com.sriramanappindi.openwire.ui.theme.OpenWireTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenWireTheme {
                val viewModel: NewsViewModel = viewModel(
                    factory = NewsViewModel.factory(applicationContext)
                )
                MainScreen(viewModel)
            }
        }
    }
}
