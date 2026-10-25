package com.kampplus.hava.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kampplus.hava.SehirListesiEkrani
import com.kampplus.hava.ui.forecast.ForecastScreen

@Composable
fun HavaNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "city",
        modifier = modifier
    ) {
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
                onGeriClick = { navController.popBackStack() }
            )
        }
    }
}