package com.livingtrust.app.presentation.pdf

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
 * States available in the Living Trust template engine.
 * Washington State (WA) is the default — first in the list.
 */
data class StateOption(val code: String, val name: String)

val ALL_TRUST_STATES = listOf(
    StateOption("WA", "Washington"),
    StateOption("CA", "California"),
    StateOption("TX", "Texas"),
    StateOption("FL", "Florida"),
    StateOption("NY", "New York"),
    StateOption("IL", "Illinois"),
    StateOption("GA", "Georgia"),
    StateOption("PA", "Pennsylvania"),
    StateOption("OH", "Ohio"),
    StateOption("NC", "North Carolina"),
    StateOption("AZ", "Arizona"),
    StateOption("NV", "Nevada"),
)

/**
 * UI state for the PDF Preview screen.
 *
 * WHY separate states for loading vs error vs success?
 * - Using a sealed class for status is more explicit than nullable fields.
 * - The UI can do an exhaustive `when` check and always render the correct state.
 */
sealed class PdfPreviewStatus {
    object Idle : PdfPreviewStatus()
    object Loading : PdfPreviewStatus()
    data class Ready(
        val governingLaw: String,
        val stateName: String,
        val documentSections: List<String>,
        val base64Pdf: String
    ) : PdfPreviewStatus()
    data class Error(val message: String) : PdfPreviewStatus()
}

data class PdfPreviewState(
    val selectedState: StateOption = ALL_TRUST_STATES.first(), // Washington is default
    val status: PdfPreviewStatus = PdfPreviewStatus.Idle,
    val pricing: PricingInfo = PricingInfo(),
)

data class PricingInfo(
    val displayPrice: String = "\$29.99",
    val amount: Int = 2999,
    val currency: String = "usd"
)

/**
 * ViewModel for the PDF Preview screen.
 *
 * WHY make HTTP calls directly in the ViewModel instead of through Retrofit?
 * - The PDF endpoints return binary/base64 data not easily typed as Retrofit DTOs.
 * - HttpURLConnection is sufficient for these one-off calls.
 * - In production, consider a dedicated PdfRepository using Retrofit's @Streaming.
 *
 * WHY store base64 in state instead of saving to disk?
 * - For preview purposes, base64 is sufficient to pass to the WebView.
 * - For final download, the user opens a browser URL — no need to store on device.
 */
@HiltViewModel
class PdfPreviewViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(PdfPreviewState())
    val state: StateFlow<PdfPreviewState> = _state.asStateFlow()

    private val baseUrl = BuildConfig.BASE_URL.trimEnd('/')

    init {
        fetchPricingConfig()
    }

    fun selectState(state: StateOption) {
        _state.value = _state.value.copy(
            selectedState = state,
            status = PdfPreviewStatus.Idle  // reset preview when state changes
        )
    }

    /**
     * Fetches pricing configuration from the backend.
     * Falls back to default pricing if the call fails.
     */
    fun fetchPricingConfig() {
        viewModelScope.launch {
            try {
                val url = URL("$baseUrl/api/payments/config")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "application/json")
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000

                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(body)
                    val price = json.getJSONObject("price")
                    _state.value = _state.value.copy(
                        pricing = PricingInfo(
                            displayPrice = price.optString("displayPrice", "\$29.99"),
                            amount = price.optInt("amount", 2999),
                            currency = price.optString("currency", "usd")
                        )
                    )
                }
                conn.disconnect()
            } catch (_: Exception) {
                // Use default pricing
            }
        }
    }

    /**
     * Generates a watermarked PDF preview from the backend.
     * The backend returns a base64-encoded PDF.
     */
    fun generatePreview(trust: Trust) {
        viewModelScope.launch {
            _state.value = _state.value.copy(status = PdfPreviewStatus.Loading)
            try {
                val selectedState = _state.value.selectedState
                val payload = buildTrustJson(trust, selectedState.code)

                val url = URL("$baseUrl/api/pdf/preview-base64")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 30_000
                conn.readTimeout = 30_000

                OutputStreamWriter(conn.outputStream).use { it.write(payload) }

                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(body)

                    _state.value = _state.value.copy(
                        status = PdfPreviewStatus.Ready(
                            governingLaw = json.optString("governingLaw", ""),
                            stateName = json.optString("state", selectedState.name),
                            base64Pdf = json.optString("base64", ""),
                            documentSections = listOf(
                                "Title Page — Trust Name & Summary",
                                "Article I — Identification & Governing Law",
                                "Article II — Revocability & Amendment",
                                "Article III — Trustee Provisions & Powers",
                                "Article IV — Distributions During Lifetime",
                                "Article V — Distribution Upon Death",
                                "Article VI — General Provisions",
                                "Execution & Signature Page",
                                "Notarization Block",
                                "Schedule A — Trust Property"
                            )
                        )
                    )
                } else {
                    val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                    val errorJson = runCatching { JSONObject(errorBody).optString("error", errorBody) }.getOrDefault(errorBody)
                    _state.value = _state.value.copy(
                        status = PdfPreviewStatus.Error("Server error: $errorJson")
                    )
                }
                conn.disconnect()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    status = PdfPreviewStatus.Error(
                        e.localizedMessage ?: "Failed to generate preview. Check your connection."
                    )
                )
            }
        }
    }

    private fun buildTrustJson(trust: Trust, stateCode: String): String {
        return JSONObject().apply {
            put("trustName", trust.trustName)
            put("grantor", trust.grantor)
            put("trustee", trust.trustee)
            put("successorTrustee", trust.successorTrustee)
            put("beneficiaries", JSONArray(trust.beneficiaries))
            put("assets", JSONArray(trust.assets))
            put("state", stateCode)
        }.toString()
    }
}
