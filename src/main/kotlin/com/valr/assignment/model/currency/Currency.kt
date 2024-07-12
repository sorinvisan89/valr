package com.valr.assignment.model.currency

enum class Currency(val code: String) {

    BTCUSDC("BTCUSDC"),

    BTCZAR("BTCZAR");

    companion object {
        fun fromString(value: String): Currency? {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) }
        }
    }
}