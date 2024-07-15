package com.valr.assignment.utils

import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantReadWriteLock

interface Locker {

    fun <T> withReadLock(block: () -> T): T

    fun <T> withWriteLock(block: () -> T): T
}

@Component
class LockerImpl : Locker {

    private val lock = ReentrantReadWriteLock()
    private val readLock = lock.readLock()
    private val writeLock = lock.writeLock()

    override fun <T> withReadLock(block: () -> T): T {
        try {
            readLock.lock()
            return block()
        } finally {
            readLock.unlock()
        }
    }

    override fun <T> withWriteLock(block: () -> T): T {
        try {
            writeLock.lock()
            return block()
        } finally {
            writeLock.unlock()
        }
    }
}
