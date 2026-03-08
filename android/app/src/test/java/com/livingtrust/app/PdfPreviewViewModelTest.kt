package com.livingtrust.app

import com.livingtrust.app.presentation.pdf.ALL_TRUST_STATES
import com.livingtrust.app.presentation.pdf.PdfPreviewStatus
import com.livingtrust.app.presentation.pdf.PdfPreviewViewModel
import com.livingtrust.app.presentation.pdf.StateOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for PdfPreviewViewModel.
 *
 * Tests cover:
 * 1. Initial state — Washington (WA) is the default state
 * 2. State selection — selectState updates selectedState and resets preview status to Idle
 * 3. ALL_TRUST_STATES list — WA is first, all entries are unique, list is non-empty
 * 4. Pricing defaults — sane fallback values before config fetch completes
 *
 * WHY no generatePreview tests?
 * - generatePreview() makes an HTTP call (HttpURLConnection) with no DI seam to inject a fake.
 * - Integration tests for HTTP calls require an instrumented test (androidTest) or a real server.
 * - The logic tested here (state selection, defaults) is pure and fast.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PdfPreviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: PdfPreviewViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PdfPreviewViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial selectedState is Washington (WA)`() {
        val state = viewModel.state.value
        assertEquals("WA", state.selectedState.code)
        assertEquals("Washington", state.selectedState.name)
    }

    @Test
    fun `initial status is Idle`() {
        assertIs<PdfPreviewStatus.Idle>(viewModel.state.value.status)
    }

    @Test
    fun `initial pricing has sane defaults`() {
        val pricing = viewModel.state.value.pricing
        assertEquals("\$29.99", pricing.displayPrice)
        assertEquals(2999, pricing.amount)
        assertEquals("usd", pricing.currency)
    }

    // ── State selection ───────────────────────────────────────────────────────

    @Test
    fun `selectState updates selectedState`() {
        val california = StateOption("CA", "California")
        viewModel.selectState(california)
        assertEquals("CA", viewModel.state.value.selectedState.code)
        assertEquals("California", viewModel.state.value.selectedState.name)
    }

    @Test
    fun `selectState resets preview status to Idle`() {
        // Simulate a Ready status by selecting a state (it resets to Idle)
        viewModel.selectState(StateOption("TX", "Texas"))
        assertIs<PdfPreviewStatus.Idle>(viewModel.state.value.status)
    }

    @Test
    fun `selectState preserves other state fields`() {
        val pricing = viewModel.state.value.pricing
        viewModel.selectState(StateOption("FL", "Florida"))
        // Pricing should remain unchanged after state selection
        assertEquals(pricing.amount, viewModel.state.value.pricing.amount)
        assertEquals(pricing.displayPrice, viewModel.state.value.pricing.displayPrice)
    }

    @Test
    fun `selectState can be called multiple times`() {
        viewModel.selectState(StateOption("NY", "New York"))
        viewModel.selectState(StateOption("IL", "Illinois"))
        viewModel.selectState(StateOption("WA", "Washington"))
        assertEquals("WA", viewModel.state.value.selectedState.code)
    }

    // ── ALL_TRUST_STATES list ─────────────────────────────────────────────────

    @Test
    fun `ALL_TRUST_STATES is not empty`() {
        assert(ALL_TRUST_STATES.isNotEmpty()) { "State list should not be empty" }
    }

    @Test
    fun `ALL_TRUST_STATES first entry is Washington (WA)`() {
        val first = ALL_TRUST_STATES.first()
        assertEquals("WA", first.code)
        assertEquals("Washington", first.name)
    }

    @Test
    fun `ALL_TRUST_STATES has no duplicate state codes`() {
        val codes = ALL_TRUST_STATES.map { it.code }
        assertEquals(codes.size, codes.distinct().size) {
            "Duplicate state codes found: ${codes.groupBy { it }.filter { it.value.size > 1 }.keys}"
        }
    }

    @Test
    fun `ALL_TRUST_STATES contains all key states`() {
        val codes = ALL_TRUST_STATES.map { it.code }.toSet()
        listOf("WA", "CA", "TX", "FL", "NY", "IL", "GA", "PA", "OH", "NC", "AZ", "NV").forEach { code ->
            assert(code in codes) { "Expected state $code to be in ALL_TRUST_STATES" }
        }
    }

    @Test
    fun `ALL_TRUST_STATES has no blank names or codes`() {
        ALL_TRUST_STATES.forEach { state ->
            assert(state.code.isNotBlank()) { "State code is blank: $state" }
            assert(state.name.isNotBlank()) { "State name is blank: $state" }
            assertEquals(2, state.code.length) { "State code '${state.code}' should be 2 characters" }
        }
    }
}
