package org.example.jobjournaler.service

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.example.jobjournaler.dto.UserDTO
import org.example.jobjournaler.entity.User
import org.example.jobjournaler.mapper.UserMapper
import org.example.jobjournaler.repository.UserCrudRepository
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun `should create a new user if the user for a given email does not exist`() = runTest {
        // Given
        val userDTO = mockk<UserDTO>()
        val user = mockk<User>()
        val email = "tester@tester.com"
        val userToSave = User(null, email)

        coEvery { userCrudRepositoryMockk.findByEmail(email) } returns null
        coEvery { userCrudRepositoryMockk.save(userToSave) } returns user
        every { userMapperMockk.toDto(user) } returns userDTO

        // When
        val result = userService.getUserByEmail(email)

        // Then
        coVerify(exactly = 1) { userCrudRepositoryMockk.findByEmail(email) }
        coVerify(exactly = 1) { userCrudRepositoryMockk.save(userToSave) }
        verify(exactly = 1) { userMapperMockk.toDto(user) }

        assertEquals(userDTO, result)
    }

}
