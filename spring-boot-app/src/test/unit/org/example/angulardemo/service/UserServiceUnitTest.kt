package org.example.angulardemo.service

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.example.angulardemo.dto.UserDTO
import org.example.angulardemo.entity.User
import org.example.angulardemo.exception.UserNotFoundException
import org.example.angulardemo.mapper.UserMapper
import org.example.angulardemo.repository.UserCrudRepository
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class UserServiceUnitTest(
    @RelaxedMockK
    private val userCrudRepositoryMockk: UserCrudRepository,
    @RelaxedMockK
    private val userMapperMockk: UserMapper,
    @InjectMockKs
    private val userService: UserService,
) {

    @Test
    fun `should find a user for an existing email`() = runTest {
        // Given
        val user = mockk<User>()
        val userDTO = mockk<UserDTO>()
        val email = "tester@tester.com"

        coEvery { userCrudRepositoryMockk.findByEmail(email) } returns user
        every { userMapperMockk.toDto(user) } returns userDTO

        // When
        val result = userService.getUserByEmail(email)

        // Then
        coVerify(exactly = 1) { userCrudRepositoryMockk.findByEmail(email) }
        verify(exactly = 1) { userMapperMockk.toDto(user) }

        assertEquals(userDTO, result)
    }

    @Test
    fun `should throw if the user for a given email does not exist`() = runTest {
        // Given
        val email = "tester@tester.com"

        coEvery { userCrudRepositoryMockk.findByEmail(email) } returns null

        // When
        assertFailsWith<UserNotFoundException> { userService.getUserByEmail(email) }

        // Then
        coVerify(exactly = 1) { userCrudRepositoryMockk.findByEmail(email) }
        verify { userMapperMockk wasNot called }
    }

}
