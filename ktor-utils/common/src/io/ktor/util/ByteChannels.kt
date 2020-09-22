/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*

private const val CHUNK_BUFFER_SIZE = 4096L

/**
 * Split source [ByteReadChannel] into 2 new one.
 * Cancel of one channel in split(input or both outputs) cancels other channels.
 */
@KtorExperimentalAPI
public fun ByteReadChannel.split(): Pair<ByteReadChannel, ByteReadChannel> {
    val first = ByteChannel(autoFlush = true)
    val second = ByteChannel(autoFlush = true)

    GlobalScope.launch {
        try {
            while (!isClosedForRead) {
                coroutineScope {
                    this@split.readRemaining(CHUNK_BUFFER_SIZE).use { chunk ->
                        val firstAsync = async { first.writePacket(chunk.copy()) }
                        val secondAsync = async { second.writePacket(chunk.copy()) }
                        firstAsync.await()
                        secondAsync.await()
                    }
                }
            }
        } catch (cause: Throwable) {
            this@split.cancel(cause)
            first.cancel(cause)
            second.cancel(cause)
        } finally {
            first.close()
            second.close()
        }
    }

    return first to second
}

/**
 * Copy source channel to both output channels chunk by chunk.
 */
@InternalAPI
public fun ByteReadChannel.copyToBoth(first: ByteWriteChannel, second: ByteWriteChannel) {
    GlobalScope.launch(Dispatchers.Unconfined) {
        try {
            while (!isClosedForRead && (!first.isClosedForWrite || !second.isClosedForWrite)) {
                readRemaining(CHUNK_BUFFER_SIZE).use {
                    try {
                        first.writePacket(it.copy())
                        second.writePacket(it.copy())
                    } catch (cause: Throwable) {
                        this@copyToBoth.cancel(cause)
                        first.close(cause)
                        second.close(cause)
                    }
                }
            }
        } catch (cause: Throwable) {
            first.close(cause)
            second.close(cause)
        } finally {
            first.close()
            second.close()
        }
    }
}


/**
 * Read channel to byte array.
 */
public suspend fun ByteReadChannel.toByteArray(): ByteArray = readRemaining().readBytes()
