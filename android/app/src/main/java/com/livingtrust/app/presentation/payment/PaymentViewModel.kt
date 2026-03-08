package com.livingtrust.app.presentation.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livingtrust.app.BuildConfig
import com.livingtrust.app.domain.model.Trust
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/**
 * Sealed class representing every possible state of the payment screen.
 *
 * WHY sealed class instead of boolean flags?
 * - With sealed classes, the UI does an exhaustive `when` check.
 *   You can't accidentally show the success UI while still loading.
 * - Each state carries only the data it needs (e.g., Success has a token; Form has card fields).
 */
sealed class PaymentUiState {
    /** Initial state — user fills in card details. */
    data class Form(
        val nameOnCard: String = "",
        val email: String = "",
        val cardNumber: String = "",
        val expiry: String = "",
        val cvc: String = "",
        val paymentIntentId: String = "",
        val clientSecret: String = "",
        val isInitializing: Boolean = true,
        val initError: String? = null
    ) : PaymentUiState()

    /** User pressed Pay — waiting for backend confirmation. */
    object Processing : PaymentUiState()

    /** Payment confirmed — download token received. */
    data class Success(
        val downloadToken: String,
        val downloadUrl: String,
        val trustName: String,
        val displayPrice: String
    ) : PaymentUiState()

    /** Something went wrong. */
    data class Error(val message: String) : PaymentUiState()
}

/**
 * ViewModel for the Stripe payment screen.
 *
 * WHY handle payment logic in the ViewModel?
 * - Payment state (card fields, intent ID, token) must survive screen rotation.
 * - The ViewModel is the only layer that survives configuration changes without restarting.
 *
 * Production note:
 * - In production, integrate the Stripe Android SDK (com.stripe:stripe-android).
 * - Use PaymentSheet or CardInputWidget from the Stripe SDK for PCI-compliant card capture.
 * - Call stripe.confirmPayment(clientSecret, confirmParams) from the ViewModel.
 * - The current implementation uses a backend mock for development/testing.
 */
@HiltViewModel
class PaymentViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<PaymentUiState>(PaymentUiState.Form())
    val state: StateFlow<PaymentUiState> = _state.asStateFlow()

    private val baseUrl = BuildConfig.BASE_URL.trimEnd('/')

    /** Initialize the payment intent when the screen opens. */
    fun initialize(trustName: String, amount: Int) {
        viewModelScope.launch {
            createPaymentIntent(trustName)
        }
    }

    private suspend fun createPaymentIntent(trustName: String) {
        val currentForm = _state.value as? PaymentUiState.Form ?: return
        _state.value = currentForm.copy(isInitializing = true, initError = null)

        try {
            val payload = JSONObject().apply {
                put("trustName", trustName)
            }.toString()

            val url = URL("$baseUrl/api/payments/create-intent")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000

            OutputStreamWriter(conn.outputStream).use { it.write(payload) }

            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(body)
                _state.value = currentForm.copy(
                    paymentIntentId = json.optString("paymentIntentId", ""),
                    clientSecret = json.optString("clientSecret", ""),
                    isInitializing = false
                )
            } else {
                val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                _state.value = currentForm.copy(
                    isInitializing = false,
                    initError = "Payment setup failed. Please try again."
                )
            }
            conn.disconnect()
        } catch (e: Exception) {
            _state.value = ((_state.value as? PaymentUiState.Form) ?: PaymentUiState.Form()).copy(
                isInitializing = false,
                initError = e.localizedMessage ?: "Network error"
            )
        }
    }

    // ── Form field updaters ──

    fun updateNameOnCard(value: String) {
        (_state.value as? PaymentUiState.Form)?.let {
            _state.value = it.copy(nameOnCard = value)
        }
    }

    fun updateEmail(value: String) {
        (_state.value as? PaymentUiState.Form)?.let {
            _state.value = it.copy(email = value)
        }
    }

    fun updateCardNumber(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(16)
        val formatted = digits.chunked(4).joinToString(" ")
        (_state.value as? PaymentUiState.Form)?.let {
            _state.value = it.copy(cardNumber = formatted)
        }
    }

    fun updateExpiry(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(4)
        val formatted = if (digits.length >= 3) "${digits.take(2)}/${digits.drop(2)}" else digits
        (_state.value as? PaymentUiState.Form)?.let {
            _state.value = it.copy(expiry = formatted)
        }
    }

    fun updateCvc(value: String) {
        val digits = value.filter { it.isDigit() }.take(4)
        (_state.value as? PaymentUiState.Form)?.let {
            _state.value = it.copy(cvc = digits)
        }
    }

    /**
     * Validates form fields and returns an error string, or null if valid.
     */
    fun validate(form: PaymentUiState.Form): String? {
        if (form.nameOnCard.isBlank()) return "Please enter the name on your card."
        if (form.email.isBlank() || !form.email.contains('@')) return "Please enter a valid email address."
        if (form.cardNumber.replace(" ", "").length < 16) return "Please enter a complete 16-digit card number."
        if (form.expiry.length < 5) return "Please enter a valid expiry date (MM/YY)."
        if (form.cvc.length < 3) return "Please enter a valid 3-digit CVC."
        return null
    }

    /**
     * Submits the payment by calling the backend /api/payments/confirm endpoint.
     *
     * Production integration:
     * 1. Call Stripe Android SDK: stripe.confirmPayment(clientSecret, cardParams)
     * 2. On SDK success, call /api/payments/confirm with the real paymentIntentId
     * 3. Receive downloadToken and show success screen
     */
    fun submitPayment(trust: Trust, stateCode: String, displayPrice: String) {
        val form = _state.value as? PaymentUiState.Form ?: return
        val validationError = validate(form)
        if (validationError != null) {
            _state.value = PaymentUiState.Error(validationError)
            return
        }

        viewModelScope.launch {
            _state.value = PaymentUiState.Processing

            try {
                val trustJson = JSONObject().apply {
                    put("trustName", trust.trustName)
                    put("grantor", trust.grantor)
                    put("trustee", trust.trustee)
                    put("successorTrustee", trust.successorTrustee)
                    put("beneficiaries", JSONArray(trust.beneficiaries))
                    put("assets", JSONArray(trust.assets))
                    put("state", stateCode)
                }

                val payload = JSONObject().apply {
                    put("paymentIntentId", form.paymentIntentId)
                    put("trustData", trustJson)
                }.toString()

                val url = URL("$baseUrl/api/payments/confirm")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 30_000
                conn.readTimeout = 30_000

                OutputStreamWriter(conn.outputStream).use { it.write(payload) }

                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(body)
                    val token = json.optString("downloadToken", "")
                    val downloadUrl = "$baseUrl/api/pdf/download/$token"

                    _state.value = PaymentUiState.Success(
                        downloadToken = token,
                        downloadUrl = downloadUrl,
                        trustName = trust.trustName,
                        displayPrice = displayPrice
                    )
                } else {
                    val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                    val errorMsg = runCatching { JSONObject(errorBody).optString("error", errorBody) }
                        .getOrDefault(errorBody)
                    _state.value = PaymentUiState.Error("Payment failed: $errorMsg")
                }
                conn.disconnect()
            } catch (e: Exception) {
                _state.value = PaymentUiState.Error(
                    e.localizedMessage ?: "Network error. Please check your connection and try again."
                )
            }
        }
    }

    /** Reset to form state (for retry). */
    fun retry() {
        _state.value = PaymentUiState.Form()
    }
}
