package com.kampplus.hava.model

fun gunlukNotlar(gun: DailyWeather): List<String> {
    val durum = WeatherCondition.fromWeatherCode(gun.weatherCode)
    val sicaklikFarki = gun.tempMax - gun.tempMin

    return buildList {
        when (durum) {
            WeatherCondition.THUNDERSTORM ->
                add("⛈️ Fırtına bekleniyor, dışarıda uzun süre kalmamaya çalış")
            WeatherCondition.SNOW ->
                add("❄️ Kar yağışı var, kaymaz ayakkabı giy")
            WeatherCondition.FOG ->
                add("🌫️ Sis var, araç kullanırken dikkatli ol")
            else -> Unit
        }

        if (durum != WeatherCondition.SNOW) {
            if (gun.precipitationProbability >= 60) {
                add("☂️ Şemsiyeni mutlaka yanına al")
            } else if (gun.precipitationProbability >= 30) {
                add("🌂 Yanına şemsiye almakta fayda var")
            }
        }

        if (gun.tempMin <= 0) {
            add("🧤 Don riski var, eldiven ve bere unutma")
        }

        if (gun.tempMax < 10) {
            add("🧥 Kalın bir mont giy")
        } else if (gun.tempMax < 18) {
            add("🧶 Yanına hırka ya da ince bir ceket al")
        } else if (sicaklikFarki >= 10) {
            add("🌙 Akşam serinleyecek, yanına hırka al")
        }

        if (gun.tempMax >= 30) {
            add("💧 Çok sıcak, bol su iç")
        } else if (gun.tempMax >= 25) {
            add("👕 İnce ve açık renkli giyin")
        }

        if (gun.uvIndexMax >= 6) {
            add("🧴 Güneş kremi sürmeyi unutma")
        }

        if (gun.windSpeedMax >= 40) {
            add("💨 Kuvvetli rüzgâr var, şemsiye yerine yağmurluk tercih et")
        } else if (gun.windSpeedMax >= 25) {
            add("🌬️ Rüzgârlı bir gün olacak")
        }

        if (isEmpty()) {
            add("😊 Güzel bir gün, keyfini çıkar")
        }
    }
}