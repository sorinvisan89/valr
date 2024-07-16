package com.valr.assignment.repository

import com.valr.assignment.model.user.Role
import com.valr.assignment.model.user.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepository(
    private val encoder: PasswordEncoder
) {

    private val users = mutableSetOf(
        User(
            id = UUID.randomUUID(),
            email = "first@gmail.com",
            password = encoder.encode("pass1"),
            role = Role.USER,
        ),
        User(
            id = UUID.randomUUID(),
            email = "second@gmail.com",
            password = encoder.encode("pass2"),
            role = Role.ADMIN,
        ),
        User(
            id = UUID.randomUUID(),
            email = "thrid@gmail.com",
            password = encoder.encode("pass3"),
            role = Role.USER,
        ),
    )

    fun save(user: User): Boolean {
        val updated = user.copy(password = encoder.encode(user.password))

        return users.add(updated)
    }

    fun findByEmail(email: String): User? =
        users
            .firstOrNull { it.email == email }

    fun findAll(): Set<User> =
        users

    fun findByUUID(uuid: UUID): User? =
        users
            .firstOrNull { it.id == uuid }

    fun deleteByUUID(uuid: UUID): Boolean {
        val foundUser = findByUUID(uuid)

        return foundUser?.let {
            users.removeIf {
                it.id == uuid
            }
        } ?: false
    }
}
