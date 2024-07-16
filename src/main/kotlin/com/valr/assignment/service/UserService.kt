package com.valr.assignment.service

import com.valr.assignment.model.user.User
import com.valr.assignment.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun createUser(user: User): User? {
        val found = userRepository.findByEmail(user.email)

        return if (found == null) {
            userRepository.save(user)
            user
        } else null
    }

    fun findByUUID(uuid: UUID): User? =
        userRepository.findByUUID(uuid)

    fun findAll(): List<User> =
        userRepository.findAll()
            .toList()

    fun deleteByUUID(uuid: UUID): Boolean =
        userRepository.deleteByUUID(uuid)
}
