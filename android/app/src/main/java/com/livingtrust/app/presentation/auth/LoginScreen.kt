package com.livingtrust.app.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Login screen — the first screen shown to unauthenticated users.
 *
 * WHY @Composable instead of a Fragment or Activity?
 * - Jetpack Compose uses @Composable functions as the building blocks of UI.
 *   Unlike Fragments (XML + Java/Kotlin code), Composables are purely Kotlin functions
 *   that describe the UI declaratively — "what it should look like given this state".
 * - When state changes, Compose automatically re-executes ("recomposes") only the parts
 *   of the UI that depend on the changed state.
 *
 * WHY does LoginScreen receive callback lambdas (onLoginSuccess, onNavigateToRegister)?
 * - This is the "event up, state down" pattern in Compose.
 * - LoginScreen doesn't know HOW to navigate — it just reports events upward.
 * - NavGraph provides the lambdas that actually call navController.navigate().
 * - This makes LoginScreen independent of the navigation system — easier to test and reuse.
 *
 * WHY `viewModel: AuthViewModel = hiltViewModel()`?
 * - hiltViewModel() is a Compose extension that gets (or creates) the ViewModel
 *   scoped to the current NavBackStackEntry — the correct lifecycle for a screen.
 * - The default value means: in production, Hilt creates the ViewModel automatically.
 *   In tests, you can pass a mock ViewModel without Hilt.
 * - @HiltViewModel on AuthViewModel + hiltViewModel() here is the complete Hilt-Compose pairing.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // collectAsState() converts StateFlow<AuthState> into a Compose State<AuthState>.
    // WHY `by` delegate? It unwraps the State wrapper so `state` is directly AuthState,
    // not State<AuthState>. This keeps the code cleaner: `state.isLoading` vs `state.value.isLoading`.
    val state by viewModel.state.collectAsState()

    // Local UI state for the text fields.
    // WHY remember { mutableStateOf("") }?
    // - `remember` keeps the value alive across recompositions (without it, the field
    //   would reset to "" every time any part of the UI recomposes).
    // - mutableStateOf("") wraps the string so Compose tracks changes and recomposes
    //   only when the email/password value actually changes.
    // - This is UI-only state (not business state), so it lives here rather than in the ViewModel.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // WHY LaunchedEffect(state.isLoggedIn)?
    // - Navigation (calling onLoginSuccess) is a side effect — it shouldn't happen
    //   inside the main composable body, which can re-run at any time.
    // - LaunchedEffect runs its block in a coroutine, keyed on state.isLoggedIn.
    //   When isLoggedIn changes to true, the block executes once and calls onLoginSuccess().
    // - WHY key on isLoggedIn? If we keyed on Unit or true, the effect would run on every
    //   recomposition. Keying on the specific value that triggers navigation is precise.
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoginSuccess()
    }

    // Column: a vertical layout container (top to bottom).
    // WHY fillMaxSize()? Takes up the entire screen.
    // WHY Arrangement.Center? Vertically centers the login form in the screen.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App title and subtitle
        Text(
            text = "Living Trust App",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary  // uses the navy blue from Theme.kt
        )
        Text(
            text = "AI-Powered Estate Planning",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,  // muted gray from Theme.kt
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Email input field.
        // WHY OutlinedTextField? Material Design 3 style with a visible border.
        // WHY KeyboardType.Email? Shows the @ key and email suggestions on the mobile keyboard.
        // WHY singleLine = true? Email and password are always single-line inputs.
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password input field.
        // WHY PasswordVisualTransformation()? Masks characters with dots so the password
        //   isn't visible to bystanders (standard security UX for password fields).
        // WHY KeyboardType.Password? Disables autocomplete and autocorrect on the mobile keyboard.
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        // Error message — only shown when state.error is non-null.
        // WHY `state.error?.let { ... }`? The `let` block only executes when error != null.
        // This is Kotlin's idiomatic null-safe conditional execution.
        state.error?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign In button.
        // WHY `enabled = !state.isLoading`?
        // - Disables the button while a request is in progress, preventing duplicate submissions.
        // - Material3 Button automatically applies a dimmed appearance when disabled.
        Button(
            onClick = { viewModel.login(email, password) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            // WHY show a spinner inside the button instead of a separate loading screen?
            // - Inline loading feedback is less disruptive than a full-screen overlay.
            //   The user can see the button context ("Sign In" → loading spinner) and
            //   understands the app is working, not frozen.
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign In")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation to Register screen.
        // WHY TextButton? Less prominent than the main Button — register is a secondary action.
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }
    }
}
