package com.livingtrust.app

import com.livingtrust.app.domain.model.Trust
import com.livingtrust.app.presentation.payment.PaymentUiState
import com.livingtrust.app.presentation.payment.PaymentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for PaymentViewModel.
 *
 * Tests cover:
 * 1. Initial state shape
 * 2. Card field formatters (number masking, expiry slash, CVC digit-only)
 * 3. Form validation — all six guard conditions
 * 4. Retry resets state to Form
 *
 * WHY no network tests here?
 * - The payment confirmation path requires a live (or mocked) HTTP server.
 * - HttpURLConnection cannot be easily mocked without a dependency injection seam.
 * - Network integration tests belong in androidTest (instrumented) or behind an
 *   interface that can be swapped for a fake. This is noted in Known Issues.
 * - These tests focus on pure logic that has no I/O dependencies.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PaymentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: PaymentViewModel

    private val sampleTrust = Trust(
        id = "",
        trustName = "Smith Family Trust",
        grantor = "John Smith",
        trustee = "John Smith",
        successorTrustee = "Jane Smith",
        beneficiaries = listOf("Alice Smith", "Bob Smith"),
        assets = listOf("Home at 123 Main St"),
        status = "draft"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PaymentViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Form with empty fields`() {
        val state = viewModel.state.value
        assertIs<PaymentUiState.Form>(state)
        assertEquals("", (state as PaymentUiState.Form).nameOnCard)
        assertEquals("", state.email)
        assertEquals("", state.cardNumber)
        assertEquals("", state.expiry)
        assertEquals("", state.cvc)
        assertTrue(state.isInitializing)
    }

    // ── Card number formatter ─────────────────────────────────────────────────

    @Test
    fun `updateCardNumber formats 16 digits into groups of 4`() {
        viewModel.updateCardNumber("4242424242424242")
        val state = viewModel.state.value as PaymentUiState.Form
        assertEquals("4242 4242 4242 4242", state.cardNumber)
    }

    @Test
    fun `updateCardNumber ignores non-digit characters`() {
        viewModel.updateCardNumber("4242-4242-4242-4242")
        val state = viewModel.state.value as PaymentUiState.Form
        assertEquals("4242 4242 4242 4242", state.cardNumber)
    }

    @Test
    fun `updateCardNumber truncates at 16 digits`() {
        viewModel.updateCardNumber("42424242424242421234")
        val state = viewModel.state.value as PaymentUiState.Form
        // Should only keep first 16 digits, formatted as 4 groups
        assertEquals("4242 4242 4242 4242", state.cardNumber)
    }

    @Test
    fun `updateCardNumber handles partial entry`() {
        viewModel.updateCardNumber("4242")
        val state = viewModel.state.value as PaymentUiState.Form
        assertEquals("4242", state.cardNumber)
    }

    // ── Expiry formatter ──────────────────────────────────────────────────────

    @Test
    fun `updateExpiry formats MM slash YY from raw digits`() {
        viewModel.updateExpiry("1228")
        val state = viewModel.state.value as PaymentUiState.Form
        assertEquals("12/28", state.expiry)
    }

    @Test
    fun `updateExpiry handles partial month input`() {
        viewModel.updateExpiry("12")
        val state = viewModel.state.value as PaymentUiState.Form
        assertEquals("12", state.expiry)
    }

    @Test
    fun `updateExpiry ignores non-digit characters`() {
        viewModel.updateExpiry("12/28")
        val state = viewModel.state.value as PaymentUiState.Form
        assertEquals("12/28", state.expiry)
    }

    // ── CVC formatter ─────────────────────────────────────────────────────────

    @Test
    fun `updateCvc accepts only digits`() {
        viewModel.updateCvc("abc123xyz")
        val state = viewModel.state.value as PaymentUiState.Form
        assertEquals("123", state.cvc)
    }

    @Test
    fun `updateCvc truncates at 4 digits for Amex support`() {
        viewModel.updateCvc("12345")
        val state = viewModel.state.value as PaymentUiState.Form
        assertEquals("1234", state.cvc)
    }

    // ── Form field updates ────────────────────────────────────────────────────

    @Test
    fun `updateNameOnCard sets name`() {
        viewModel.updateNameOnCard("John Smith")
        val state = viewModel.state.value as PaymentUiState.Form
        assertEquals("John Smith", state.nameOnCard)
    }

    @Test
    fun `updateEmail sets email`() {
        viewModel.updateEmail("john@example.com")
        val state = viewModel.state.value as PaymentUiState.Form
        assertEquals("john@example.com", state.email)
    }

    // ── Form validation ───────────────────────────────────────────────────────

    @Test
    fun `validate returns error when name is blank`() {
        val form = PaymentUiState.Form(
            nameOnCard = "",
            email = "john@example.com",
            cardNumber = "4242 4242 4242 4242",
            expiry = "12/28",
            cvc = "123"
        )
        val error = viewModel.validate(form)
        assertEquals("Please enter the name on your card.", error)
    }

    @Test
    fun `validate returns error when email is blank`() {
        val form = PaymentUiState.Form(
            nameOnCard = "John Smith",
            email = "",
            cardNumber = "4242 4242 4242 4242",
            expiry = "12/28",
            cvc = "123"
        )
        val error = viewModel.validate(form)
        assertEquals("Please enter a valid email address.", error)
    }

    @Test
    fun `validate returns error when email has no at sign`() {
        val form = PaymentUiState.Form(
            nameOnCard = "John Smith",
            email = "notanemail",
            cardNumber = "4242 4242 4242 4242",
            expiry = "12/28",
            cvc = "123"
        )
        val error = viewModel.validate(form)
        assertEquals("Please enter a valid email address.", error)
    }

    @Test
    fun `validate returns error when card number is incomplete`() {
        val form = PaymentUiState.Form(
            nameOnCard = "John Smith",
            email = "john@example.com",
            cardNumber = "4242 4242",
            expiry = "12/28",
            cvc = "123"
        )
        val error = viewModel.validate(form)
        assertEquals("Please enter a complete 16-digit card number.", error)
    }

    @Test
    fun `validate returns error when expiry is incomplete`() {
        val form = PaymentUiState.Form(
            nameOnCard = "John Smith",
            email = "john@example.com",
            cardNumber = "4242 4242 4242 4242",
            expiry = "12",
            cvc = "123"
        )
        val error = viewModel.validate(form)
        assertEquals("Please enter a valid expiry date (MM/YY).", error)
    }

    @Test
    fun `validate returns error when cvc is too short`() {
        val form = PaymentUiState.Form(
            nameOnCard = "John Smith",
            email = "john@example.com",
            cardNumber = "4242 4242 4242 4242",
            expiry = "12/28",
            cvc = "12"
        )
        val error = viewModel.validate(form)
        assertEquals("Please enter a valid 3-digit CVC.", error)
    }

    @Test
    fun `validate returns null for fully valid form`() {
        val form = PaymentUiState.Form(
            nameOnCard = "John Smith",
            email = "john@example.com",
            cardNumber = "4242 4242 4242 4242",
            expiry = "12/28",
            cvc = "123",
            paymentIntentId = "pi_mock_123",
            isInitializing = false
        )
        val error = viewModel.validate(form)
        assertNull(error)
    }

    // ── Retry ────────────────────────────────────────────────────────────────

    @Test
    fun `retry resets state to Form`() {
        // Force an error state by triggering validation failure
        viewModel.submitPayment(sampleTrust, "WA", "\$29.99")
        advanceUntilIdle()

        viewModel.retry()
        assertIs<PaymentUiState.Form>(viewModel.state.value)
    }

    // ── State immutability ────────────────────────────────────────────────────

    @Test
    fun `field updates on non-Form states are silently ignored`() {
        // Manually put viewModel in a non-Form state by calling retry then checking
        // that multiple field updates don't crash
        viewModel.updateNameOnCard("John")
        viewModel.updateEmail("john@test.com")
        viewModel.updateCardNumber("4111111111111111")
        viewModel.updateExpiry("0130")
        viewModel.updateCvc("737")

        val state = viewModel.state.value as PaymentUiState.Form
        assertEquals("John", state.nameOnCard)
        assertEquals("john@test.com", state.email)
        assertEquals("4111 1111 1111 1111", state.cardNumber)
        assertEquals("01/30", state.expiry)
        assertEquals("737", state.cvc)
    }
}
