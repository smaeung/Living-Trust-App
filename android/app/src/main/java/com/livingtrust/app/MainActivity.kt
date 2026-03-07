package com.livingtrust.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.livingtrust.app.presentation.navigation.NavGraph
import com.livingtrust.app.ui.theme.LivingTrustTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity — the single Activity that hosts the entire app.
 *
 * WHY only one Activity?
 * Modern Android development uses the "Single Activity" pattern.
 * Instead of creating a new Activity for each screen, we have ONE Activity
 * and swap Composable screens inside it using Navigation Compose.
 * This is simpler, uses less memory, and enables smooth screen transitions.
 *
 * WHY @AndroidEntryPoint?
 * This Hilt annotation tells Hilt that this Activity can receive injected
 * dependencies. It also enables @HiltViewModel to work inside this Activity —
 * without it, calling hiltViewModel() in a Composable screen would crash.
 *
 * What happens in onCreate():
 * 1. super.onCreate() — Android framework setup (always call this first)
 * 2. enableEdgeToEdge() — lets the app draw behind the status bar and
 *    navigation bar for a full-screen modern look
 * 3. setContent { } — replaces the traditional XML layout with Jetpack Compose.
 *    Everything inside is a Composable (Kotlin function that draws UI).
 * 4. LivingTrustTheme { } — wraps all screens with our custom colours and fonts
 * 5. NavGraph() — the navigation controller that decides which screen is shown
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LivingTrustTheme {
                // NavGraph is the root of our navigation.
                // It starts at the Login screen and routes the user
                // to Home after successful authentication.
                NavGraph()
            }
        }
    }
}
