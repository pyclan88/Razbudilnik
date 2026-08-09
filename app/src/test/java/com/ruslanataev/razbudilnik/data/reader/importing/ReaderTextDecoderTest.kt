package com.ruslanataev.razbudilnik.data.reader.importing

import com.ruslanataev.razbudilnik.domain.reader.models.ReaderTextEncoding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.charset.Charset

class ReaderTextDecoderTest {

    private val decoder = ReaderTextDecoder()

    @Test
    fun `detects UTF-8 BOM`() {
        val utf8Bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        val fileBytes = utf8Bom + "Hello".toByteArray(Charsets.UTF_8)

        val result = decoder.detectEncoding(fileBytes)

        assertEquals(ReaderTextEncoding.UTF_8, result)
    }

    @Test
    fun `detects valid UTF-8 text without BOM`() {
        val fileBytes = "Привет".toByteArray(Charsets.UTF_8)

        val result = decoder.detectEncoding(fileBytes)

        assertEquals(ReaderTextEncoding.UTF_8, result)
    }

    @Test
    fun `does not guess encoding for non UTF-8 text`() {
        val fileBytes =
            "Привет".toByteArray(Charset.forName(ReaderTextEncoding.WINDOWS_1251.charsetName))

        val result = decoder.detectEncoding(fileBytes)

        assertNull(result)
    }

    @Test
    fun `decodes Windows-1251 text when selected`() {
        val fileBytes =
            "Привет".toByteArray(Charset.forName(ReaderTextEncoding.WINDOWS_1251.charsetName))

        val result = decoder.decode(fileBytes, ReaderTextEncoding.WINDOWS_1251)

        assertEquals("Привет", result)
    }

    @Test
    fun `removes UTF-8 BOM while decoding`() {
        val utf8Bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        val fileBytes = utf8Bom + "Hello".toByteArray(Charsets.UTF_8)

        val result = decoder.decode(fileBytes, ReaderTextEncoding.UTF_8)

        assertEquals("Hello", result)
    }

    @Test
    fun `detects UTF-16 little-endian BOM`() {
        val utf16LeBom = byteArrayOf(0xFF.toByte(), 0xFE.toByte())

        val result = decoder.detectEncoding(utf16LeBom)

        assertEquals(ReaderTextEncoding.UTF_16_LE, result)
    }

    @Test
    fun `detects UTF-16 big-endian BOM`() {
        val utf16BeBom = byteArrayOf(0xFE.toByte(), 0xFF.toByte())

        val result = decoder.detectEncoding(utf16BeBom)

        assertEquals(ReaderTextEncoding.UTF_16_BE, result)
    }

    @Test
    fun `decodes UTF-16 little-endian text after BOM`() {
        val utf16LeBom = byteArrayOf(0xFF.toByte(), 0xFE.toByte())
        val charset = Charset.forName(ReaderTextEncoding.UTF_16_LE.charsetName)
        val fileBytes = utf16LeBom + "Привет".toByteArray(charset)

        val result = decoder.decode(fileBytes, ReaderTextEncoding.UTF_16_LE)

        assertEquals("Привет", result)
    }
}
