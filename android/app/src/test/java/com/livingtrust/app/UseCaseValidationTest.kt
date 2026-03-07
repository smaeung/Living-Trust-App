package com.livingtrust.app

import com.livingtrust.app.domain.model.Trust
import com.livingtrust.app.domain.repository.AuthRepository
import com.livingtrust.app.domain.repository.AiRepository
import com.livingtrust.app.domain.repository.TrustRepository
import com.livingtrust.app.domain.usecase.ai.ChatWithAiUseCase
import com.livingtrust.app.domain.usecase.auth.LoginUseCase
import com.livingtrust.app.domain.usecase.auth.RegisterUseCase
import com.livingtrust.app.domain.usecase.trust.CreateTrustUseCase
import com.livingtrust.app.util.Resource
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UseCaseValidationTest {

    private val authRepository: AuthRepository = mockk()
    private val trustRepository: TrustRepository = mockk()
    private val aiRepository: AiRepository = mockk()

    @Test
    fun `LoginUseCase returns error for blank email`() = runBlocking {
        val useCase = LoginUseCase(authRepository)
        val result = useCase("", "password123")
        assertIs<Resource.Error>(result)
        assertEquals("Email is required", (result as Resource.Error).message)
    }

    @Test
    fun `LoginUseCase returns error for blank password`() = runBlocking {
        val useCase = LoginUseCase(authRepository)
        val result = useCase("test@example.com", "")
        assertIs<Resource.Error>(result)
        assertEquals("Password is required", (result as Resource.Error).message)
    }

    @Test
    fun `RegisterUseCase returns error for short password`() = runBlocking {
        val useCase = RegisterUseCase(authRepository)
        val result = useCase("John", "john@example.com", "12345")
        assertIs<Resource.Error>(result)
        assertEquals("Password must be at least 6 characters", (result as Resource.Error).message)
    }

    @Test
    fun `RegisterUseCase returns error for blank name`() = runBlocking {
        val useCase = RegisterUseCase(authRepository)
        val result = useCase("", "john@example.com", "password123")
        assertIs<Resource.Error>(result)
        assertEquals("Name is required", (result as Resource.Error).message)
    }

    @Test
    fun `CreateTrustUseCase returns error for blank trust name`() = runBlocking {
        val useCase = CreateTrustUseCase(trustRepository)
        val trust = Trust(
            trustName = "",
            grantor = "John",
            trustee = "John",
            successorTrustee = "Jane",
            beneficiaries = emptyList(),
            assets = emptyList()
        )
        val result = useCase(trust)
        assertIs<Resource.Error>(result)
        assertEquals("Trust name is required", (result as Resource.Error).message)
    }

    @Test
    fun `ChatWithAiUseCase returns error for blank message`() = runBlocking {
        val useCase = ChatWithAiUseCase(aiRepository)
        val result = useCase("   ")
        assertIs<Resource.Error>(result)
        assertEquals("Message cannot be empty", (result as Resource.Error).message)
    }
}
