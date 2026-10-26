package com.kampplus.hava.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Arkadaki renk ve efektlerin hafifçe göründüğü "buzlu cam" kart. */
@Composable
fun CamKart(
    modifier: Modifier = Modifier,
    icerik: @Composable ColumnScope.() -> Unit
) {
    val sekil = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(sekil)
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.25f), sekil)
            .padding(16.dp),
        content = icerik
    )
}

/** Renkli arka plan üzerinde kullanılan yarı saydam, hap şeklinde düğme. */
@Composable
fun CamDugme(
    yazi: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sekil = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(sekil)
            .background(Color.White.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.35f), sekil)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = yazi,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Önemli eylemler için beyaz, gölgeli, dikkat çeken düğme. */
@Composable
fun ParlakDugme(
    yazi: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    vurgu: Color = GokMavisi
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = vurgu
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Text(
            text = yazi,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
