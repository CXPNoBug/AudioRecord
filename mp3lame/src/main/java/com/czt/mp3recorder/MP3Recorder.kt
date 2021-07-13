package com.czt.mp3recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

/**
 * <pre>
 *     author : ChengPeng
 *     e-mail : cxpnobug@gmail.com
 *     time   : 2021/07/13
 *     desc   : MP3 录制
 *     version: 1.0
 * </pre>
 */
class MP3Recorder {

    private val mAudioRecord: AudioRecord? = null
    private val mBufferSize: Int = 0
    private val mPCMBuffer: ShortArray? = null


    companion object {
        //=======================AudioRecord Default Settings=======================
        //麦克风收音
        private const val DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC

        /**
         * 以下三项为默认配置参数。Google Android文档明确表明只有以下3个参数是可以在所有设备上保证支持的。
         */
        private const val DEFAULT_SAMPLING_RATE = 44100 //模拟器仅支持从麦克风输入8kHz采样率
        private const val DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

        //======================Lame Default Settings=====================
        //MP3音频质量。0~9。 其中0是最好，非常慢，9是最差。
        private const val DEFAULT_LAME_MP3_QUALITY = 7

        /**
         * 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
         */
        private const val DEFAULT_LAME_IN_CHANNEL = 1

        /**
         * Encoded bit rate. MP3 file will be encoded with bit rate 32kbps
         */
        private const val DEFAULT_LAME_MP3_BIT_RATE = 32

        //自定义 每160帧作为一个周期，通知一下需要进行编码
        private const val FRAME_COUNT = 160
    }
}