package com.livingtrust.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.livingtrust.app.presentation.ai.AiAssistantScreen
import com.livingtrust.app.presentation.auth.LoginScreen
import com.livingtrust.app.presentation.auth.RegisterScreen
import com.livingtrust.app.presentation.documents.DocumentsScreen
import com.livingtrust.app.presentation.home.HomeScreen
import com.livingtrust.app.presentation.settings.SettingsScreen
import com.livingtrust.app.presentation.trust.TrustWizardScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val TRUST_WIZARD = "trust_wizard"
    const val AI_ASSISTANT = "ai_assistant"
    const val DOCUMENTS = "documents"
    const val SETTINGS = "settings"
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToTrustWizard = { navController.navigate(Routes.TRUST_WIZARD) },
                onNavigateToAiAssistant = { navController.navigate(Routes.AI_ASSISTANT) },
                onNavigateToDocuments = { navController.navigate(Routes.DOCUMENTS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.TRUST_WIZARD) {
            TrustWizardScreen(
                onNavigateBack = { navController.popBackStack() },
                onTrustCreated = { navController.popBackStack() }
            )
        }

        composable(Routes.AI_ASSISTANT) {
            AiAssistantScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DOCUMENTS) {
            DocumentsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
