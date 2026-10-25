package com.kampplus.hava

import android.os.Bundle
import android.util.Log
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
import com.kampplus.hava.ui.theme.HavaTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.IconButton
import com.kampplus.hava.data.CityRepository
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Lifecycle", "onCreate")
        enableEdgeToEdge()
        setContent {
            HavaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SehirListesiEkrani(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Lifecycle", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("Lifecycle", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Lifecycle", "onStop")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Merhaba $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HavaTheme {
        Greeting("Sümeyra")
    }
}
@Composable
fun SayacEkrani(modifier: Modifier = Modifier) {
    var count by rememberSaveable { mutableStateOf(0) }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Merhaba Kamp!")
        Text(text = "Sayaç: $count")
        Button(onClick = { count++ }) {
            Text(text = "Artır")
        }
        Spacer(modifier = Modifier.height(16.dp))
        SehirKarti(sehirAdi = "Elazığ", plaka = "23")
        Spacer(modifier = Modifier.height(8.dp))
        SehirKarti(sehirAdi = "Ankara", plaka = "06")
    }
}

@Preview(showBackground = true)
@Composable
fun SayacEkraniPreview() {
    HavaTheme {
        SayacEkrani()
    }
}
@Composable
fun SehirKarti( sehirAdi: String,
                plaka: String,
                modifier: Modifier = Modifier,
                secili: Boolean = false,
                onClick: () -> Unit = {}) {
    val arkaPlanRengi = if (secili) Color(0xFF90CAF9) else Color(0xFFE3F2FD)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(arkaPlanRengi)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF1565C0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = plaka, color = Color.White)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = sehirAdi)
    }
}



@Composable
fun SehirListesiEkrani(modifier: Modifier = Modifier) {
    var seciliSehir by rememberSaveable { mutableStateOf<String?>(null) }
    var aramaMetni by rememberSaveable { mutableStateOf("") }

    val repository = remember { CityRepository() }
    val filtrelenmisSehirler = repository.sehirAra(aramaMetni)

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Merhaba Kamp!",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = aramaMetni,
            onValueChange = { yeniMetin -> aramaMetni = yeniMetin },
            label = { Text(text = "Şehir ara") },
            singleLine = true,
            trailingIcon = {
                if (aramaMetni.isNotEmpty()) {
                    IconButton(onClick = { aramaMetni = "" }) {
                        Text(text = "✕")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (filtrelenmisSehirler.isEmpty()) {
            Text(
                text = "Şehir bulunamadı",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                text = "${filtrelenmisSehirler.size} şehir bulundu",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtrelenmisSehirler) { sehir ->
                    SehirKarti(
                        sehirAdi = sehir.ad,
                        plaka = sehir.plaka,
                        secili = sehir.ad == seciliSehir,
                        onClick = {
                            seciliSehir = sehir.ad
                            Log.d("SehirListesi", "Tıklandı: ${sehir.ad}")
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SehirKartiPreview() {
    HavaTheme {
        SehirKarti(sehirAdi = "Elazığ", plaka = "23")
    }
}