package com.kampplus.hava.data

import android.location.Address
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

data class Konum(
    val latitude: Double,
    val longitude: Double,
    val ad: String,
    val ulkeKodu: String?
)

object SonKonum {
    var deger: Konum? = null
}

class KonumRepository(private val context: Context) {

    private val konumServisi = LocationServices.getFusedLocationProviderClient(context)

    fun izinVarMi(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun mevcutKonumuAl(): Konum? {
        if (!izinVarMi()) return null

        val konum = konumServisi
            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .await()
            ?: konumServisi.lastLocation.await()
            ?: return null

        val adres = adresiBul(konum.latitude, konum.longitude)
        val parcalar = listOfNotNull(
            adres?.subLocality ?: adres?.locality,
            adres?.subAdminArea
        ).distinct()
        val ad = if (parcalar.isEmpty()) "Konumum" else parcalar.joinToString(", ")

        val sonuc = Konum(konum.latitude, konum.longitude, ad, adres?.countryCode)
        SonKonum.deger = sonuc
        return sonuc
    }

    private suspend fun adresiBul(latitude: Double, longitude: Double): Address? =
        withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext null

                @Suppress("DEPRECATION")
                Geocoder(context, Locale.forLanguageTag("tr-TR"))
                    .getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull()
            } catch (e: Exception) {
                null
            }
        }
}
fun mesafeKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val sonuc = FloatArray(1)
    android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, sonuc)
    return sonuc[0] / 1000.0
}