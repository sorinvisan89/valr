package com.valr.assignment.service

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import java.util.*

interface IdGenerator {

    fun generate(): String
}

@Service
class DefaultIdGenerator : IdGenerator {
    override fun generate(): String = UUID.randomUUID().toString()
}