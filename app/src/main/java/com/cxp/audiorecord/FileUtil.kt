package com.cxp.audiorecord

import java.io.File

/**
 * <pre>
 *     author : ChengPeng
 *     e-mail : cxpnobug@gmail.com
 *     time   : 2021/07/12
 *     desc   : 文件工具类
 *     version: 1.0
 * </pre>
 */
object FileUtil {
    private val ROOT_PATH = AudioRecordApplication.context.filesDir.absolutePath + "/audio_record"
    val AUDIO_PCM_PATH = "$ROOT_PATH/pcm/"
    val AUDIO_WAV_PATH = "$ROOT_PATH/wav/"
    val AUDIO_MP3_PATH = "$ROOT_PATH/mp3/"

    /**
     * 获取Pcm文件路径
     */
    fun getPcmFilePath(fileName: String): String {
        if (fileName.isBlank()) {
            throw NullPointerException("file name is blank.")
        }
        var newFileName = ""
        if (!fileName.endsWith(".pcm")) {
            newFileName = "$fileName.pcm"
        }
        val fileDir = File(AUDIO_PCM_PATH)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        return "$AUDIO_PCM_PATH$newFileName"
    }

    /**
     * 获取Wav文件路径
     */
    fun getWavFilePath(fileName: String): String {
        if (fileName.isBlank()) {
            throw NullPointerException("file name is blank.")
        }
        var newFileName = ""
        if (!fileName.endsWith(".wav")) {
            newFileName = "$fileName.wav"
        }
        val fileDir = File(AUDIO_WAV_PATH)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        return "$AUDIO_WAV_PATH$newFileName"
    }

    /**
     * 获取Mp3文件路径
     */
    fun getMp3FilePath(fileName: String): String {
        if (fileName.isBlank()) {
            throw NullPointerException("file name is blank.")
        }
        var newFileName = ""
        if (!fileName.endsWith(".mp3")) {
            newFileName = "$fileName.mp3"
        }
        val fileDir = File(AUDIO_MP3_PATH)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        return "$AUDIO_MP3_PATH$newFileName"
    }

}