package com.cxp.audiorecord

import android.util.Log
import java.io.*


/**
 * <pre>
 *     author : ChengPeng
 *     e-mail : cxpnobug@gmail.com
 *     time   : 2021/07/13
 *     desc   : Pcm 转 Wav 工具类
 *     version: 1.0
 * </pre>
 */
object PcmToWav {

    private const val TAG = "CXP_LOG"

    /**
     * 合并多个 pcm 文件为一个 wav 文件
     * @param filePaths pcm文件路径集合
     * @param destinationPath 目标wav文件路径
     * @return
     */
    fun mergePcmFilesToWavFile(filePaths: List<String>, destinationPath: String): Boolean {
        val buffer: ByteArray
        var totalSize = 0L
        filePaths.forEach {
            totalSize += File(it).length()
        }
        // 填入参数，比特率等等。这里用的是16位单声道 8000 hz
        val header = WaveHeader()
        //长度字段 = 内容的大小（TOTAL_SIZE) + 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
        header.fileLength = totalSize + (44 - 8)
        header.fmtHdrLeth = 16
        header.bitsPerSample = 16
        header.channels = 1
        header.formatTag = 0x0001
        header.samplesPerSec = 16000
        header.blockAlign = header.channels * header.bitsPerSample / 8
        header.avgBytesPerSec = header.blockAlign * header.samplesPerSec
        header.dataHdrLeth = totalSize

        try {
            val h = header.getHeader() as ByteArray
            // WAV标准，头部应该是44字节,如果不是44个字节则不进行转换文件
            if (h.size != 44) return false

            //先删除目标文件
            val destFile = File(destinationPath)
            if (destFile.exists()) {
                destFile.delete()
            }
            //合成所有的pcm文件的数据，写到目标文件
            buffer = ByteArray(1024 * 4)
            var input: InputStream? = null
            val output = BufferedOutputStream(FileOutputStream(destinationPath))
            output.use {
                output.write(h, 0, h.size)
                filePaths.forEach {
                    input = BufferedInputStream(FileInputStream(it))
                    input?.let { ins ->
                        ins.use {
                            var bytes = ins.read(buffer)
                            while (bytes >= 0) {
                                output.write(buffer)
                                output.flush()
                                bytes = ins.read(buffer)
                            }
                        }
                    }
                }
            }
            //清除文件
            clearFiles(filePaths)
            return true
        } catch (e: Exception) {
            Log.d(TAG, "mergePcmFilesToWavFile: ${e.message}")
            return false
        }
    }

    /**
     * 将一个pcm文件转化为wav文件
     * @param pcmPath pcm文件路径
     * @param destinationPath 目标文件路径(wav)
     * @param deletePcmFile 是否删除源文件
     * @return
     */
    fun makePcmFileToWavFile(
        pcmPath: String,
        destinationPath: String,
        deletePcmFile: Boolean
    ): Boolean {
        val buffer: ByteArray
        var totalSize = 0L
        val file = File(pcmPath)
        if (!file.exists()) {
            return false
        }
        totalSize = file.length()
        // 填入参数，比特率等等。这里用的是16位单声道 8000 hz
        val header = WaveHeader()
        //长度字段 = 内容的大小（TOTAL_SIZE) + 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
        header.fileLength = totalSize + (44 - 8)
        header.fmtHdrLeth = 16
        header.bitsPerSample = 16
        header.channels = 1
        header.formatTag = 0x0001
        header.samplesPerSec = 16000
        header.blockAlign = header.channels * header.bitsPerSample / 8
        header.avgBytesPerSec = header.blockAlign * header.samplesPerSec
        header.dataHdrLeth = totalSize

        try {
            val h = header.getHeader() as ByteArray
            // WAV标准，头部应该是44字节,如果不是44个字节则不进行转换文件
            if (h.size != 44) return false

            //先删除目标文件
            val destFile = File(destinationPath)
            if (destFile.exists()) {
                destFile.delete()
            }
            //合成所有的pcm文件的数据，写到目标文件
            buffer = ByteArray(1024 * 4)
            val input: InputStream
            val output = BufferedOutputStream(FileOutputStream(destinationPath))
            output.use {
                output.write(h, 0, h.size)
                input = BufferedInputStream(FileInputStream(file))
                input.use {
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer)
                        output.flush()
                        bytes = input.read(buffer)
                    }
                }
            }
            if (deletePcmFile) {
                file.delete()
            }
            return true
        } catch (e: Exception) {
            Log.d(TAG, "mergePcmFilesToWavFile: ${e.message}")
            return false
        }
    }

    /**
     * 清除文件
     */
    private fun clearFiles(filePaths: List<String>) {
        filePaths.forEach {
            val file = File(it)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}