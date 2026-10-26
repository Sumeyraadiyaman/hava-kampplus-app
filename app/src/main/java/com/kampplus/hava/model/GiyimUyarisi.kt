package com.kampplus.hava.model

import kotlin.math.roundToInt

enum class UyariTuru {
    FIRTINA, SEMSIYE, KAR, DON, SOGUK, SICAK, GUNES_KREMI, RUZGAR, HIRKA, GUZEL
}

data class GiyimUyarisi(
    val tur: UyariTuru,
    val emoji: String,
    val baslik: String,
    val aciklama: String
)

/**
 * O günün EN ÖNEMLİ uyarısını seçer (ekranın üstünde büyük bant olarak gösterilir).
 * Sıralama önemli: daha tehlikeli durumlar önce kontrol edilir.
 */
fun anaUyari(gun: DailyWeather): GiyimUyarisi {
    val durum = WeatherCondition.fromWeatherCode(gun.weatherCode)

    return when {
        durum == WeatherCondition.THUNDERSTORM -> GiyimUyarisi(
            UyariTuru.FIRTINA, "⛈️", "Fırtına uyarısı!",
            "Dışarıda uzun kalma. Şemsiye yerine yağmurluk al."
        )
        durum == WeatherCondition.SNOW -> GiyimUyarisi(
            UyariTuru.KAR, "❄️", "Kar yağışı var",
            "Kalın mont, bere ve kaymaz ayakkabı giy."
        )
        gun.precipitationProbability >= 50 -> GiyimUyarisi(
            UyariTuru.SEMSIYE, "☂️", "Şemsiyeni unutma!",
            "Yağmur ihtimali %${gun.precipitationProbability}."
        )
        gun.tempMin <= 0 -> GiyimUyarisi(
            UyariTuru.DON, "🧤", "Don riski var",
            "Eldiven, bere ve atkı ile çık."
        )
        gun.tempMax < 10 -> GiyimUyarisi(
            UyariTuru.SOGUK, "🧥", "Hava soğuk",
            "Kalın bir mont giymeyi unutma."
        )
        gun.tempMax >= 30 -> GiyimUyarisi(
            UyariTuru.SICAK, "🥵", "Çok sıcak!",
            "İnce ve açık renkli giyin, bol su iç."
        )
        gun.windSpeedMax >= 40 -> GiyimUyarisi(
            UyariTuru.RUZGAR, "💨", "Kuvvetli rüzgâr",
            "Şemsiye yerine yağmurluk tercih et."
        )
        gun.uvIndexMax >= 6 -> GiyimUyarisi(
            UyariTuru.GUNES_KREMI, "🧴", "Güneş kremi sür",
            "UV indeksi yüksek (${gun.uvIndexMax.roundToInt()})."
        )
        gun.tempMax < 18 || gun.tempMax - gun.tempMin >= 10 -> GiyimUyarisi(
            UyariTuru.HIRKA, "🧶", "Yanına hırka al",
            "Gün içinde hava serinleyecek."
        )
        else -> GiyimUyarisi(
            UyariTuru.GUZEL, "😎", "Harika bir gün!",
            "Hafif giyin ve keyfini çıkar."
        )
    }
}

/**
 * O gün için geçerli TÜM önerileri, önem sırasına göre döndürür.
 * Ekranda sağa kaydırılan kartlar olarak gösterilir.
 */
fun tumOneriler(gun: DailyWeather): List<GiyimUyarisi> {
    val durum = WeatherCondition.fromWeatherCode(gun.weatherCode)
    val sicaklikFarki = gun.tempMax - gun.tempMin

    val liste = buildList {
        if (durum == WeatherCondition.THUNDERSTORM) {
            add(GiyimUyarisi(UyariTuru.FIRTINA, "⛈️", "Fırtına uyarısı!", "Dışarıda uzun kalma, yağmurluk tercih et."))
        }
        if (durum == WeatherCondition.SNOW) {
            add(GiyimUyarisi(UyariTuru.KAR, "❄️", "Kar yağacak", "Kalın mont, bere ve kaymaz ayakkabı giy."))
        } else if (gun.precipitationProbability >= 60) {
            add(GiyimUyarisi(UyariTuru.SEMSIYE, "☂️", "Şemsiyeni unutma!", "Yağmur ihtimali %${gun.precipitationProbability}."))
        } else if (gun.precipitationProbability >= 30) {
            add(GiyimUyarisi(UyariTuru.SEMSIYE, "🌂", "Şemsiye alabilirsin", "Yağmur ihtimali %${gun.precipitationProbability}."))
        }
        if (gun.tempMin <= 0) {
            add(GiyimUyarisi(UyariTuru.DON, "🧤", "Don riski var", "Eldiven, bere ve atkını unutma."))
        }
        when {
            gun.tempMax < 10 -> add(GiyimUyarisi(UyariTuru.SOGUK, "🧥", "Kalın bir mont giy", "En yüksek ${gun.tempMax.roundToInt()}°, hava soğuk."))
            gun.tempMax < 18 -> add(GiyimUyarisi(UyariTuru.HIRKA, "🧶", "Yanına hırka al", "Gün boyu serin, en yüksek ${gun.tempMax.roundToInt()}°."))
            sicaklikFarki >= 10 -> add(GiyimUyarisi(UyariTuru.HIRKA, "🌙", "Akşam serinleyecek", "Gece ${gun.tempMin.roundToInt()}°'ye düşecek, hırka al."))
        }
        if (gun.tempMax >= 30) {
            add(GiyimUyarisi(UyariTuru.SICAK, "🥵", "Çok sıcak!", "İnce ve açık renkli giyin, bol su iç."))
        } else if (gun.tempMax >= 25) {
            add(GiyimUyarisi(UyariTuru.GUZEL, "👕", "Hafif giyin", "Tişört havası, en yüksek ${gun.tempMax.roundToInt()}°."))
        }
        if (gun.uvIndexMax >= 6) {
            add(GiyimUyarisi(UyariTuru.GUNES_KREMI, "🧴", "Güneş kremi ve gözlük", "UV indeksi ${gun.uvIndexMax.roundToInt()}, güneşten korun."))
        }
        if (gun.windSpeedMax >= 30) {
            add(GiyimUyarisi(UyariTuru.RUZGAR, "💨", "Rüzgârlı bir gün", "Rüzgâr ${gun.windSpeedMax.roundToInt()} km/sa'e kadar çıkacak."))
        }
    }

    return liste.ifEmpty {
        listOf(GiyimUyarisi(UyariTuru.GUZEL, "😎", "Harika bir gün!", "Hafif giyin ve keyfini çıkar."))
    }
}
