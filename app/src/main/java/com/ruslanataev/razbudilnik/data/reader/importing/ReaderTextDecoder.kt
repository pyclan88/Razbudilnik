package com.ruslanataev.razbudilnik.data.reader.importing

import com.ruslanataev.razbudilnik.domain.reader.models.ReaderTextEncoding
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

internal class ReaderTextDecoder {

    fun detectEncoding(fileBytes: ByteArray): ReaderTextEncoding? {
        return when {
            fileBytes.hasPrefix(UTF_8_BOM) -> ReaderTextEncoding.UTF_8
            fileBytes.hasPrefix(UTF_16_LE_BOM) -> ReaderTextEncoding.UTF_16_LE
            fileBytes.hasPrefix(UTF_16_BE_BOM) -> ReaderTextEncoding.UTF_16_BE
            fileBytes.isValidUtf8() -> ReaderTextEncoding.UTF_8
            else -> null
        }
    }

    fun decode(
        fileBytes: ByteArray,
        encoding: ReaderTextEncoding,
    ): String {
        val contentBytes = fileBytes.copyOfRange(
            fromIndex = fileBytes.bomLength(),
            toIndex = fileBytes.size,
        )

        return contentBytes.toString(Charset.forName(encoding.charsetName))
    }

    private fun ByteArray.hasPrefix(prefix: ByteArray): Boolean {
        return size >= prefix.size && prefix.indices.all { index ->
            this[index] == prefix[index]
        }
    }

    private fun ByteArray.isValidUtf8(): Boolean {
        return runCatching {
            Charset.forName(ReaderTextEncoding.UTF_8.charsetName)
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(this))
        }.isSuccess
    }

    private fun ByteArray.bomLength(): Int {
        return when {
            hasPrefix(UTF_8_BOM) -> UTF_8_BOM.size
            hasPrefix(UTF_16_LE_BOM) -> UTF_16_LE_BOM.size
            hasPrefix(UTF_16_BE_BOM) -> UTF_16_BE_BOM.size
            else -> 0
        }
    }

    private companion object {
        val UTF_8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        val UTF_16_LE_BOM = byteArrayOf(0xFF.toByte(), 0xFE.toByte())
        val UTF_16_BE_BOM = byteArrayOf(0xFE.toByte(), 0xFF.toByte())
    }
}
