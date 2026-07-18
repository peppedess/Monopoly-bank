package com.peppedess.monopolybank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LoadingIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.peppedess.monopolybank.ui.BankViewModel
import com.peppedess.monopolybank.ui.screens.HomeScreen
import com.peppedess.monopolybank.ui.screens.PlayerDetailScreen
import com.peppedess.monopolybank.ui.screens.SetupScreen
import com.peppedess.monopolybank.ui.screens.TransferScreen
import com.peppedess.monopolybank.ui.theme.MonopolyBankTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as MonopolyApp).repository
        setContent {
            MonopolyBankTheme {
                val vm: BankViewModel = viewModel(factory = BankViewModel.factory(repo))
                AppNav(vm)
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNav(vm: BankViewModel) {
    val nav = rememberNavController()
    val hasGame by vm.hasGame.collectAsState()

    if (hasGame == null) {
        // Splash con LoadingIndicator Expressive
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
        return
    }

    val start = if (hasGame == true) "home" else "setup"

    NavHost(
        navController = nav,
        startDestination = start,
        enterTransition = {
            slideInHorizontally(tween(350)) { it / 3 } + fadeIn(tween(350))
        },
        exitTransition = { fadeOut(tween(250)) },
        popEnterTransition = { fadeIn(tween(250)) },
        popExitTransition = {
            slideOutHorizontally(tween(300)) { it / 3 } + fadeOut(tween(300))
        }
    ) {
        composable("setup") {
            SetupScreen(vm) {
                nav.navigate("home") { popUpTo("setup") { inclusive = true } }
            }
        }
        composable("home") {
            HomeScreen(
                vm,
                onPlayer = { nav.navigate("player/$it") },
                onTransfer = { nav.navigate("transfer") },
                onNewGame = {
                    nav.navigate("setup") { popUpTo("home") { inclusive = true } }
                }
            )
        }
        composable("transfer") {
            TransferScreen(vm) { nav.popBackStack() }
        }
        composable(
            "player/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            PlayerDetailScreen(vm, id) { nav.popBackStack() }
        }
    }
}
