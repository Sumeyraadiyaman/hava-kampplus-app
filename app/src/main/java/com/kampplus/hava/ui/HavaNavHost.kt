package com.kampplus.hava.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kampplus.hava.SehirListesiEkrani
import com.kampplus.hava.ui.forecast.ForecastScreen
import com.kampplus.hava.ui.forecast.ForecastViewModel
import com.kampplus.hava.ui.forecast.GunDetayEkrani

@Composable
fun HavaNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            ForecastScreen(
                onGeriClick = {},
                onAramaClick = {
                    navController.navigate("city") {
                        launchSingleTop = true
                    }
                },
                onGunSecildi = { tarih ->
                    navController.navigate("gun/$tarih") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("city") {
            SehirListesiEkrani(
                onSehirSecildi = { city ->
                    val ad = Uri.encode(city.name)
                    navController.navigate("forecast/${city.latitude}/${city.longitude}/$ad") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("forecast/{lat}/{lon}/{name}") {
            ForecastScreen(
                onGeriClick = { navController.popBackStack() },
                onGunSecildi = { tarih ->
                    navController.navigate("gun/$tarih") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("gun/{tarih}") { geriYiginKaydi ->
            val oncekiKayit = remember(geriYiginKaydi) {
                navController.previousBackStackEntry
            } ?: return@composable

            val tahminViewModel: ForecastViewModel = viewModel(oncekiKayit)
            val tarih = geriYiginKaydi.arguments?.getString("tarih") ?: ""

            GunDetayEkrani(
                tarih = tarih,
                onGeriClick = { navController.popBackStack() },
                viewModel = tahminViewModel
            )
        }
    }
}