package com.livingtrust.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extension property that creates a single DataStore instance attached to the Application context.
 *
 * WHY a Kotlin extension property?
 * `by preferencesDataStore(name = "...")` uses Kotlin's property delegation.
 * It lazily creates the DataStore on first access and caches it — so there is
 * always exactly one instance for the whole app. Multiple instances of DataStore
 * pointing to the same file would cause runtime errors.
 *
 * "living_trust_prefs" is just the file name for the preferences store on disk.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "living_trust_prefs")

/**
 * TokenManager — stores and retrieves the JWT authentication token securely.
 *
 * WHY DataStore instead of SharedPreferences?
 * SharedPreferences is the old Android way to save small key-value data, but it
 * has a critical flaw: it can block the main thread (the UI thread) while reading.
 * DataStore is the modern replacement: it is fully coroutine-based and async,
 * so it never blocks the UI.
 *
 * WHY @Singleton?
 * We want exactly one TokenManager instance for the whole app. If two different
 * parts of the app created separate instances, they could get out of sync.
 * @Singleton tells Hilt: "create this object once and reuse it everywhere".
 *
 * WHY @Inject constructor?
 * This tells Hilt how to create a TokenManager. Hilt sees that it needs a Context
 * and provides it automatically (the @ApplicationContext qualifier ensures it gets
 * the app-level context, not an Activity context which could leak memory).
 *
 * WHY JWT tokens?
 * After login the server returns a JSON Web Token (JWT). This token is sent with
 * every subsequent API request in the "Authorization: Bearer <token>" header.
 * The server uses it to know who is making the request without requiring the
 * user to enter their password every time.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // The key under which the token is stored in DataStore.
        // Using stringPreferencesKey ensures type safety — you cannot accidentally
        // read this key as an Int or Boolean.
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    /**
     * Saves the JWT token to persistent storage after a successful login or register.
     *
     * `suspend` means this function must be called from a coroutine (or another
     * suspend function). It will not block the main thread while writing to disk.
     *
     * @param token The JWT string received from the backend (e.g. "eyJhbGciOiJ...")
     */
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token  // write the new token, replacing any existing one
        }
    }

    /**
     * Reads the stored token, or returns null if the user is not logged in.
     *
     * How it works:
     * dataStore.data is a Flow (a stream of values). We use .map to extract just
     * the token from the full preferences snapshot, then .firstOrNull() to read
     * the current value once and stop — we don't need ongoing updates here.
     *
     * @return The JWT string if the user is logged in, null otherwise.
     */
    suspend fun getToken(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]  // returns null if the key doesn't exist yet
        }.firstOrNull()
    }

    /**
     * Deletes the stored token, effectively logging the user out.
     * After this call, getToken() will return null and all authenticated
     * API calls will fail until the user logs in again.
     */
    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}
