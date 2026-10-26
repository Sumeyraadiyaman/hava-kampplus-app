# Hava Durumu ☀️🌧️❄️

Bulunduğun yerin havasını açar açmaz gösteren, ne giymen gerektiğini söyleyen ve havaya göre canlanan bir Android hava durumu uygulaması.

KAMP+ Bilişim Kampı (T.C. Gençlik ve Spor Bakanlığı & Turkcell Geleceği Yazanlar) Android eğitimi kapsamında geliştirildi.

## Öne çıkanlar

- **Konumuna göre otomatik hava durumu:** Uygulama açılınca telefonun bulunduğu yerin (mahalle ve ilçe adıyla) havası gelir.
- **Canlı arka planlar:** Yağmurda damlalar düşer, karda kar yağar, fırtınada şimşek çakar, açık gecelerde yıldızlar parlar, sıcak günlerde havada buhar yükselir, ilkbaharda çiçek yaprakları ve kelebekler uçuşur.
- **Ne giymeli?** Her gün için şemsiye, hırka, mont, güneş kremi gibi öneriler. Ekran açılınca günün en önemli hatırlatması üstten kayarak gelir.
- **Spor uygunluğu:** Sıcaklık, yağış, rüzgâr, UV ve hava kalitesine göre 10 üzerinden puan ve günün en uygun saati.
- **Konumlarım:** İstediğin yerleri ekle, ana ekranda 3B geçişle kaydırarak ya da üstteki adlara dokunarak aralarında gezin. Konumlar uygulama kapansa da saklanır.
- **Hızlı konum ekleme:** 81 il yazarken anında önerilir (Türkçe karakter gerekmez: "elazig" → Elazığ). İlçe ve yurt dışı için tam adla arama yapılır, sonuçlar önce Türkiye, sonra yakınlık sırasıyla gelir.
- **Plaka ve bayrak:** Türkiye'deki yerler plakalarıyla, diğer ülkeler bayraklarıyla gösterilir.
- **Detaylı tahmin:** 24 saatlik saatlik tahmin, 7 ve 15 günlük tahmin, nem, rüzgâr, basınç, UV, gün doğumu ve batımı, hava kalitesi.
- **Koyu tema desteği**

## Teknolojiler

Kotlin · Jetpack Compose (Material 3) · ViewModel + StateFlow · Coroutines · Retrofit + Gson · Navigation Compose · DataStore · Google Play Services Location

## Mimari

```
UI (Composable)  →  ViewModel (StateFlow)  →  Repository  →  Retrofit / DataStore / Konum servisi
```

```
com.kampplus.hava
├── MainActivity.kt
├── model/      Veri modelleri ve iş kuralları (günlük notlar, spor puanı, plakalar)
├── data/       API'ler, Repository'ler, konum ve kayıtlı konumlar
└── ui/
    ├── home/       Ana ekran, Konumlarım, Konum ekle
    ├── forecast/   Tahmin ve gün detay ekranları
    └── theme/      Renkler, havaya göre görünüm ve hareketli efektler
```

## Veri kaynağı

Hava durumu, şehir arama ve hava kalitesi verileri [Open-Meteo](https://open-meteo.com) servisinden alınır (anahtar gerekmez, ticari olmayan kullanım içindir).

## Çalıştırma

```
./gradlew assembleDebug
```

Android Studio ile açıp ▶️ ile emülatörde ya da telefonda çalıştırabilirsin. Emülatörde konumu **Extended Controls → Location** bölümünden ayarlayabilirsin.
