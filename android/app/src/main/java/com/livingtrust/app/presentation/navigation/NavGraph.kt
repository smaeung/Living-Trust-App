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

/**
 * Route constants for all navigation destinations in the app.
 *
 * WHY an object with string constants instead of an enum or sealed class?
 * - Navigation Compose uses string-based routes (like URLs: "home", "trust_wizard").
 *   String constants are the simplest representation.
 * - An object (Kotlin singleton) groups related constants without instantiation.
 * - WHY not just use the raw strings inline?
 *   Typos like "hme" instead of "home" would compile fine but crash at runtime.
 *   Using constants gives compile-time safety — if you rename a route, the IDE
 *   finds all usages automatically.
 *
 * WHY no arguments in the routes (e.g., "trust/{id}")?
 * - For v1, screens load their own data (e.g., HomeScreen fetches trusts itself).
 *   Passing IDs via navigation arguments is more complex and not needed yet.
 */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val TRUST_WIZARD = "trust_wizard"
    const val AI_ASSISTANT = "ai_assistant"
    const val DOCUMENTS = "documents"
    const val SETTINGS = "settings"
}

/**
 * The app's navigation graph — defines every screen and how they connect.
 *
 * WHY a single NavGraph for the entire app?
 * - This app uses the single-Activity pattern: one MainActivity hosts all screens.
 *   NavGraph defines ALL routes in one place, making the navigation structure
 *   easy to understand at a glance.
 * - For larger apps with many features, nested navigation graphs (sub-graphs) can be used.
 *
 * WHY Compose Navigation instead of Fragment-based navigation?
 * - Fragments predate Compose and have their own lifecycle complexity.
 *   Navigation Compose is designed for Composable screens — no FragmentManager needed.
 * - Routes are type-safe strings (or can be typed with Kotlin serialization in newer versions).
 *
 * WHY `navController: NavHostController = rememberNavController()`?
 * - Default parameter: in production, NavGraph creates its own NavController.
 * - In tests, a custom NavController can be injected for navigation testing.
 * - rememberNavController() is a Compose-aware constructor that creates a controller
 *   scoped to the current Composition.
 *
 * WHY `startDestination: String = Routes.LOGIN`?
 * - The app always starts at the Login screen. The default can be overridden in tests
 *   (e.g., start at HOME to test the home screen without going through login).
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    // NavHost: the container that renders the current screen based on the active route.
    // It watches navController and recomposes when the user navigates.
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Each composable() block registers one route.
        // The lambda receives the NavBackStackEntry (route-specific data) and
        // renders the appropriate screen composable.

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // Navigate to HOME and REMOVE login from the back stack.
                    // WHY popUpTo(Routes.LOGIN) { inclusive = true }?
                    // Without this, pressing the Android back button from the Home screen
                    // would return to the Login screen — bad UX after a successful login.
                    // inclusive = true means Login itself is also removed (not just routes above it).
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
                    // Same back-stack clearing pattern as login:
                    // After registering, going back should NOT return to Login or Register.
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    // User tapped "Already have an account?" — go back to Login.
                    // popBackStack() is simpler than navigate() when just going one step back.
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
                    // On logout, clear the entire back stack so back button exits the app.
                    // popUpTo(Routes.HOME) { inclusive = true } removes HOME from the stack too.
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.TRUST_WIZARD) {
            TrustWizardScreen(
                // Both "back" and "trust created" navigate to the same destination (back stack pop),
                // but they represent different events — keeping them separate is more explicit.
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
                    // Logout from Settings: clear the full back stack back to HOME, then go to LOGIN.
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
