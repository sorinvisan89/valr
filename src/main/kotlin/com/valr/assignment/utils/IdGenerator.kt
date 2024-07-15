package com.valr.assignment.service

import org.springframework.stereotype.Component
import java.util.UUID

interface IdGenerator {

    fun generate(): String
}

@Component
class IdGeneratorImpl : IdGenerator {
    override fun generate(): String = UUID.randomUUID().toString()
}
