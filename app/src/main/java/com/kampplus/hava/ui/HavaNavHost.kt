package com.kampplus.hava.ui

import com.kampplus.hava.ui.home.KonumlarimEkrani
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kampplus.hava.ui.home.KonumEkleEkrani
import com.kampplus.hava.ui.forecast.ForecastViewModel
import com.kampplus.hava.ui.forecast.GunDetayEkrani
import com.kampplus.hava.ui.home.AnaEkran
import com.kampplus.hava.ui.home.AnaEkranViewModel
import com.kampplus.hava.ui.home.KONUMUM_ANAHTARI

@Composable
fun HavaNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            AnaEkran(
                onKonumlarimClick = {
                    navController.navigate("konumlarim") {
                        launchSingleTop = true
                    }
                },
                onGunSecildi = { anahtar, tarih ->
                    navController.navigate("gun/${Uri.encode(anahtar)}/$tarih") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("konumlarim") { geriYiginKaydi ->
            val anaKayit = remember(geriYiginKaydi) {
                navController.getBackStackEntry("home")
            }
            val anaEkranViewModel: AnaEkranViewModel = viewModel(anaKayit)

            KonumlarimEkrani(
                onGeriClick = { navController.popBackStack() },
                onKonumSecildi = { sayfa ->
                    anaEkranViewModel.sayfayaGit(sayfa)
                    navController.popBackStack("home", inclusive = false)
                },
                onKonumEkleClick = {
                    navController.navigate("konum_ekle") {
                        launchSingleTop = true
                    }
                },
                viewModel = anaEkranViewModel
            )
        }

        composable("konum_ekle") {
            KonumEkleEkrani(
                onGeriClick = { navController.popBackStack() },
                onEklendi = {
                    if (navController.currentDestination?.route == "konum_ekle") {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable("gun/{anahtar}/{tarih}") { geriYiginKaydi ->
            val anaKayit = remember(geriYiginKaydi) {
                navController.getBackStackEntry("home")
            }
            val anahtar = geriYiginKaydi.arguments?.getString("anahtar") ?: KONUMUM_ANAHTARI
            val tarih = geriYiginKaydi.arguments?.getString("tarih") ?: ""
            val tahminViewModel: ForecastViewModel = viewModel(anaKayit, key = anahtar)

            GunDetayEkrani(
                tarih = tarih,
                onGeriClick = { navController.popBackStack() },
                viewModel = tahminViewModel
            )
        }
    }
}