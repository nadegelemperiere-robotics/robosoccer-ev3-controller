package org.mantabots.robosoccer.utils

import java.io.InputStream

interface FrameFactory {
    fun fromPayload(payload: ByteArray): Frame
    suspend fun fromInputStream(stream: InputStream): Frame
}