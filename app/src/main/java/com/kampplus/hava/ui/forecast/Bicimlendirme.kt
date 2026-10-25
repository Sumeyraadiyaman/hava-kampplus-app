package com.kampplus.hava.ui.forecast

import java.text.SimpleDateFormat
import java.util.Locale

private val turkce = Locale.forLanguageTag("tr-TR")

private fun tarihBicimle(tarih: String, kalip: String): String {
    return try {
        val okuyucu = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = okuyucu.parse(tarih) ?: return tarih
        SimpleDateFormat(kalip, turkce).format(date)
    } catch (e: Exception) {
        tarih
    }
}

fun tarihYaz(tarih: String): String = tarihBicimle(tarih, "EEEE, d MMMM")

fun gunEtiketi(sira: Int, tarih: String): String = when (sira) {
    0 -> "Bugün"
    1 -> "Yarın"
    else -> tarihBicimle(tarih, "EEE d MMM")
}

fun saatKismi(tarihSaat: String): String = tarihSaat.substringAfter('T', tarihSaat)

fun ruzgarYonu(derece: Int): String {
    val yonler = listOf(
        "Kuzey", "Kuzeydoğu", "Doğu", "Güneydoğu",
        "Güney", "Güneybatı", "Batı", "Kuzeybatı"
    )
    val sira = (((derece % 360) + 360 + 22.5) / 45).toInt() % 8
    return yonler[sira]
}

fun uvSeviyesi(uv: Double): String = when {
    uv < 3 -> "Düşük"
    uv < 6 -> "Orta"
    uv < 8 -> "Yüksek"
    uv < 11 -> "Çok yüksek"
    else -> "Aşırı"
}