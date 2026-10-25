package com.kampplus.hava.model

import kotlin.math.abs

data class SporUygunlugu(
    val puan: Int,
    val baslik: String,
    val aciklama: String,
    val enUygunSaat: String?
)

fun sporUygunlugu(
    gun: DailyWeather,
    saatler: List<HourlyWeather>,
    havaKalitesi: Int? = null
): SporUygunlugu {
    val durum = WeatherCondition.fromWeatherCode(gun.weatherCode)
    var puan = 10
    val nedenler = mutableListOf<String>()

    when (durum) {
        WeatherCondition.THUNDERSTORM -> {
            puan -= 5
            nedenler.add("fırtına")
        }
        WeatherCondition.SNOW -> {
            puan -= 3
            nedenler.add("kar")
        }
        WeatherCondition.RAIN, WeatherCondition.SHOWERS -> {
            puan -= 2
            nedenler.add(durum.label.lowercase())
        }
        WeatherCondition.DRIZZLE -> {
            puan -= 1
            nedenler.add("çisenti")
        }
        else -> Unit
    }

    if (gun.precipitationProbability >= 70) {
        puan -= 3
        nedenler.add("yüksek yağış ihtimali")
    } else if (gun.precipitationProbability >= 40) {
        puan -= 2
        nedenler.add("yağış ihtimali")
    }

    if (gun.tempMax >= 35) {
        puan -= 6
        nedenler.add("aşırı sıcak")
    } else if (gun.tempMax >= 32) {
        puan -= 4
        nedenler.add("çok sıcak")
    } else if (gun.tempMax >= 28) {
        puan -= 2
        nedenler.add("sıcak hava")
    } else if (gun.tempMax < 0) {
        puan -= 5
        nedenler.add("dondurucu soğuk")
    } else if (gun.tempMax < 5) {
        puan -= 3
        nedenler.add("soğuk hava")
    } else if (gun.tempMax < 10) {
        puan -= 1
        nedenler.add("serin hava")
    }

    if (gun.windSpeedMax >= 40) {
        puan -= 3
        nedenler.add("kuvvetli rüzgâr")
    } else if (gun.windSpeedMax >= 25) {
        puan -= 1
        nedenler.add("rüzgâr")
    }

    if (gun.uvIndexMax >= 8) {
        puan -= 2
        nedenler.add("çok yüksek UV")
    } else if (gun.uvIndexMax >= 6) {
        puan -= 1
        nedenler.add("yüksek UV")
    }
    if (havaKalitesi != null) {
        if (havaKalitesi >= 80) {
            puan -= 4
            nedenler.add("çok kötü hava kalitesi")
        } else if (havaKalitesi >= 60) {
            puan -= 3
            nedenler.add("kötü hava kalitesi")
        } else if (havaKalitesi >= 40) {
            puan -= 1
            nedenler.add("orta düzeyde hava kirliliği")
        }
    }
    puan = puan.coerceIn(1, 10)

    val baslik = when {
        puan >= 8 -> "Spor için çok uygun"
        puan >= 5 -> "Spor için uygun"
        else -> "Dışarıda spor için uygun değil"
    }

    val aciklama = when {
        nedenler.isEmpty() -> "Hava koşulları dışarıda spor yapmak için ideal."
        puan < 5 -> "Dikkat: ${nedenler.joinToString(", ")}. Bugün içeride spor yapmayı düşünebilirsin."
        else -> "Dikkat: ${nedenler.joinToString(", ")}."
    }

    return SporUygunlugu(
        puan = puan,
        baslik = baslik,
        aciklama = aciklama,
        enUygunSaat = enUygunSaatiBul(gun, saatler)
    )
}

private fun saatSayisi(tarihSaat: String): Int? =
    tarihSaat.substringAfter('T').take(2).toIntOrNull()

private fun enUygunSaatiBul(gun: DailyWeather, saatler: List<HourlyWeather>): String? {
    val dogus = saatSayisi(gun.sunrise) ?: 7
    val batis = saatSayisi(gun.sunset) ?: 18

    val gunduzSaatleri = saatler.filter { saat ->
        val s = saatSayisi(saat.time) ?: -1
        s in (dogus + 1) until batis
    }

    val enIyi = gunduzSaatleri.minByOrNull { saat ->
        saat.precipitationProbability * 2 +
                abs(saat.temperature - 18) * 3 +
                saat.windSpeed / 5
    }

    return enIyi?.time?.substringAfter('T')
}