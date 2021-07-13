package com.cxp.audiorecord

/**
 * <pre>
 *     author : ChengPeng
 *     e-mail : cxpnobug@gmail.com
 *     time   : 2021/07/12
 *     desc   : 获取录音的音频流,用于拓展的处理
 *     version: 1.0
 * </pre>
 */
interface RecordStreamListener {
    fun recordOfByte(data: ByteArray, begin: Int, end: Int)
}