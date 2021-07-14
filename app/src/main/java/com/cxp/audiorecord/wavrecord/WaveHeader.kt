package com.cxp.audiorecord.wavrecord

import java.io.ByteArrayOutputStream

import java.io.IOException

/**
 * <pre>
 *     author : ChengPeng
 *     e-mail : cxpnobug@gmail.com
 *     time   : 2021/07/13
 *     desc   : Wav 头信息
 *     version: 1.0
 * </pre>
 */
class WaveHeader {
    private val fileID = charArrayOf('R', 'I', 'F', 'F')
    var fileLength = 0L
    private var wavTag = charArrayOf('W', 'A', 'V', 'E')
    private var fmtHdrID = charArrayOf('f', 'm', 't', ' ')
    var fmtHdrLeth = 0L
    var formatTag: Int = 0
    var channels: Int = 0
    var samplesPerSec = 0L
    var avgBytesPerSec = 0L
     var blockAlign: Int = 0
    var bitsPerSample: Int = 0
    private var dataHdrID = charArrayOf('d', 'a', 't', 'a')
    var dataHdrLeth = 0L

    @Throws(IOException::class)
    fun getHeader(): ByteArray? {
        val bos = ByteArrayOutputStream()
        writeChar(bos, fileID)
        writeLong(bos, fileLength)
        writeChar(bos, wavTag)
        writeChar(bos, fmtHdrID)
        writeLong(bos, fmtHdrLeth)
        writeInt(bos, formatTag)
        writeInt(bos, channels)
        writeLong(bos, samplesPerSec)
        writeLong(bos, avgBytesPerSec)
        writeInt(bos, blockAlign)
        writeInt(bos, bitsPerSample)
        writeChar(bos, dataHdrID)
        writeLong(bos, dataHdrLeth)
        bos.flush()
        val r = bos.toByteArray()
        bos.close()
        return r
    }

    @Throws(IOException::class)
    private fun writeInt(bos: ByteArrayOutputStream, s: Int) {
        val mybyte = ByteArray(2)
        mybyte[1] = (s shl 16 shr 24).toByte()
        mybyte[0] = (s shl 24 shr 24).toByte()
        bos.write(mybyte)
    }


    @Throws(IOException::class)
    private fun writeLong(bos: ByteArrayOutputStream, n: Long) {
        val buf = ByteArray(4)
        buf[3] = (n shr 24).toByte()
        buf[2] = (n shl 8 shr 24).toByte()
        buf[1] = (n shl 16 shr 24).toByte()
        buf[0] = (n shl 24 shr 24).toByte()
        bos.write(buf)
    }

    private fun writeChar(bos: ByteArrayOutputStream, id: CharArray) {
        for (i in id.indices) {
            val c = id[i]
            bos.write(c.code)
        }
    }
}