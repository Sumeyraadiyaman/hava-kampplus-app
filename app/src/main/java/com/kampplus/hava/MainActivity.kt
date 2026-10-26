package com.kampplus.hava

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kampplus.hava.ui.HavaNavHost
import com.kampplus.hava.ui.theme.HavaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tüm ekranlar renkli arka plana sahip, bu yüzden saat ve pil simgeleri hep beyaz olsun
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            HavaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HavaNavHost(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
