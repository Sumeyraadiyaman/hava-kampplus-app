package com.kampplus.hava.model

import java.util.Locale

private val turkce = Locale.forLanguageTag("tr-TR")

private fun ilAdiniSadelestir(ad: String): String =
    ad.lowercase(turkce)
        .replace("â", "a")
        .replace("î", "i")
        .replace("û", "u")
        .replace("ı", "i")
        .replace("ğ", "g")
        .replace("ü", "u")
        .replace("ş", "s")
        .replace("ö", "o")
        .replace("ç", "c")
        .removeSuffix(" ili")
        .removeSuffix(" province")
        .trim()

object Plakalar {

    private val ilTablosu: Map<String, String> = mapOf(
        "Adana" to "01", "Adıyaman" to "02", "Afyonkarahisar" to "03", "Ağrı" to "04",
        "Amasya" to "05", "Ankara" to "06", "Antalya" to "07", "Artvin" to "08",
        "Aydın" to "09", "Balıkesir" to "10", "Bilecik" to "11", "Bingöl" to "12",
        "Bitlis" to "13", "Bolu" to "14", "Burdur" to "15", "Bursa" to "16",
        "Çanakkale" to "17", "Çankırı" to "18", "Çorum" to "19", "Denizli" to "20",
        "Diyarbakır" to "21", "Edirne" to "22", "Elazığ" to "23", "Erzincan" to "24",
        "Erzurum" to "25", "Eskişehir" to "26", "Gaziantep" to "27", "Giresun" to "28",
        "Gümüşhane" to "29", "Hakkari" to "30", "Hatay" to "31", "Isparta" to "32",
        "Mersin" to "33", "İstanbul" to "34", "İzmir" to "35", "Kars" to "36",
        "Kastamonu" to "37", "Kayseri" to "38", "Kırklareli" to "39", "Kırşehir" to "40",
        "Kocaeli" to "41", "Konya" to "42", "Kütahya" to "43", "Malatya" to "44",
        "Manisa" to "45", "Kahramanmaraş" to "46", "Mardin" to "47", "Muğla" to "48",
        "Muş" to "49", "Nevşehir" to "50", "Niğde" to "51", "Ordu" to "52",
        "Rize" to "53", "Sakarya" to "54", "Samsun" to "55", "Siirt" to "56",
        "Sinop" to "57", "Sivas" to "58", "Tekirdağ" to "59", "Tokat" to "60",
        "Trabzon" to "61", "Tunceli" to "62", "Şanlıurfa" to "63", "Uşak" to "64",
        "Van" to "65", "Yozgat" to "66", "Zonguldak" to "67", "Aksaray" to "68",
        "Bayburt" to "69", "Karaman" to "70", "Kırıkkale" to "71", "Batman" to "72",
        "Şırnak" to "73", "Bartın" to "74", "Ardahan" to "75", "Iğdır" to "76",
        "Yalova" to "77", "Karabük" to "78", "Kilis" to "79", "Osmaniye" to "80",
        "Düzce" to "81",
        "İçel" to "33", "Afyon" to "03"
    )

    private val tablo: Map<String, String> =
        ilTablosu.mapKeys { (ad, _) -> ilAdiniSadelestir(ad) }

    private val eskiAdlar = setOf("İçel", "Afyon")

    fun ilAra(sorgu: String): List<String> {
        val aranan = ilAdiniSadelestir(sorgu)
        if (aranan.isEmpty()) return emptyList()
        return ilTablosu.keys.filter { il ->
            il !in eskiAdlar && ilAdiniSadelestir(il).startsWith(aranan)
        }
    }

    fun bul(ilAdi: String?): String? {
        if (ilAdi == null) return null
        return tablo[ilAdiniSadelestir(ilAdi)]
    }
}

fun bayrakEmoji(ulkeKodu: String?): String {
    if (ulkeKodu == null || ulkeKodu.length != 2) return "🏳️"
    val kod = ulkeKodu.uppercase(Locale.US)
    val ilkHarf = kod[0].code - 'A'.code + 0x1F1E6
    val ikinciHarf = kod[1].code - 'A'.code + 0x1F1E6
    return String(Character.toChars(ilkHarf)) + String(Character.toChars(ikinciHarf))
}