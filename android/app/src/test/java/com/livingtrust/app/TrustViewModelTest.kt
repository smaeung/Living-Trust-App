package com.livingtrust.app

import com.livingtrust.app.domain.model.Trust
import com.livingtrust.app.domain.usecase.trust.CreateTrustUseCase
import com.livingtrust.app.presentation.trust.TrustViewModel
import com.livingtrust.app.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TrustViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var createTrustUseCase: CreateTrustUseCase
    private lateinit var viewModel: TrustViewModel

    private val validTrust = Trust(
        id = "1",
        trustName = "Smith Family Trust",
        grantor = "John Smith",
        trustee = "John Smith",
        successorTrustee = "Jane Smith",
        beneficiaries = listOf("Alice Smith"),
        assets = listOf("Home at 123 Main St"),
        status = "draft"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        createTrustUseCase = mockk()
        viewModel = TrustViewModel(createTrustUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has step 1`() {
        assertEquals(1, viewModel.state.value.step)
        assertEquals(4, viewModel.state.value.totalSteps)
    }

    @Test
    fun `nextStep increments step`() {
        viewModel.nextStep()
        assertEquals(2, viewModel.state.value.step)
    }

    @Test
    fun `previousStep does not go below 1`() {
        viewModel.previousStep()
        assertEquals(1, viewModel.state.value.step)
    }

    @Test
    fun `nextStep does not exceed totalSteps`() {
        repeat(10) { viewModel.nextStep() }
        assertEquals(viewModel.state.value.totalSteps, viewModel.state.value.step)
    }

    @Test
    fun `addBeneficiary adds to list`() {
        viewModel.addBeneficiary("Alice Smith")
        assertEquals(listOf("Alice Smith"), viewModel.state.value.beneficiaries)
    }

    @Test
    fun `removeBeneficiary removes from list`() {
        viewModel.addBeneficiary("Alice")
        viewModel.addBeneficiary("Bob")
        viewModel.removeBeneficiary(0)
        assertEquals(listOf("Bob"), viewModel.state.value.beneficiaries)
    }

    @Test
    fun `addAsset adds to list`() {
        viewModel.addAsset("House")
        assertEquals(listOf("House"), viewModel.state.value.assets)
    }

    @Test
    fun `submitTrust on success sets isComplete true`() = runTest {
        coEvery { createTrustUseCase(any()) } returns Resource.Success(validTrust)

        viewModel.updateTrustName("Smith Family Trust")
        viewModel.updateGrantor("John Smith")
        viewModel.updateTrustee("John Smith")
        viewModel.updateSuccessorTrustee("Jane Smith")

        viewModel.submitTrust()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isComplete)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `submitTrust on error sets error message`() = runTest {
        coEvery { createTrustUseCase(any()) } returns Resource.Error("Trust name is required")

        viewModel.submitTrust()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isComplete)
        assertEquals("Trust name is required", viewModel.state.value.error)
    }
}
