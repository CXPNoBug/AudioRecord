package com.czt.mp3recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.ToneGenerator
import android.util.Log
import com.czt.mp3recorder.util.LameUtil
import java.io.File
import java.io.IOException
import kotlin.math.sqrt


/**
 * <pre>
 *     author : ChengPeng
 *     e-mail : cxpnobug@gmail.com
 *     time   : 2021/07/13
 *     desc   : MP3 录制
 *     version: 1.0
 *     address: https://www.cnblogs.com/ct2011/p/4080193.html
 * </pre>
 */
class MP3Recorder(private val mRecordFile: File) {

    private var mAudioRecord: AudioRecord? = null
    private var mEncodeThread: DataEncodeThread? = null
    private var mPCMBuffer: ShortArray? = null
    private var mBufferSize: Int = 0
    private var  mIsRecording = false

    private var mVolume = 0

    @Throws(IOException::class)
    fun start() {
        if (mIsRecording) {
            return
        }
        //防止init或startRecording被多次调用
        mIsRecording = true
        initAudioRecorder()
        try {
            mAudioRecord?.startRecording()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Thread {
            //设置线程权限
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
            while (mIsRecording) {
                val readSize = mAudioRecord!!.read(mPCMBuffer!!, 0, mBufferSize)
                if (readSize > 0) {
                    mEncodeThread!!.addTask(mPCMBuffer!!, readSize)
                    calculateRealVolume(mPCMBuffer!!, readSize)
                }
            }
            // release and finalize audioRecord
            mAudioRecord!!.stop()
            mAudioRecord!!.release()
            mAudioRecord = null
            // stop the encoding thread and try to wait
            // until the thread finishes its job
            mEncodeThread!!.sendStopMessage()
        }.start()
    }

    /**
     * 初始化音频录制
     */
    @Throws(IOException::class)
    private fun initAudioRecorder() {
        mBufferSize = AudioRecord.getMinBufferSize(
            DEFAULT_SAMPLING_RATE,
            DEFAULT_CHANNEL_CONFIG,
            DEFAULT_AUDIO_FORMAT
        )
        val bytesPerFrame = DEFAULT_AUDIO_FORMAT
        //使能被整除，方便下面的周期性通知
        var frameSize = mBufferSize / bytesPerFrame
        if (frameSize % FRAME_COUNT != 0) {
            frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT)
            mBufferSize = frameSize * bytesPerFrame
        }

        mAudioRecord = AudioRecord(
            DEFAULT_AUDIO_SOURCE,
            DEFAULT_SAMPLING_RATE,
            DEFAULT_CHANNEL_CONFIG,
            DEFAULT_AUDIO_FORMAT,
            mBufferSize
        )
        mPCMBuffer = ShortArray(mBufferSize)

        /**
         * Initialize lame buffer
         * mp3 sampling rate is the same as the recorded pcm sampling rate
         * The bit rate is 32kbps
         */
        LameUtil.init(
            DEFAULT_SAMPLING_RATE,
            DEFAULT_LAME_IN_CHANNEL,
            DEFAULT_SAMPLING_RATE,
            DEFAULT_LAME_MP3_BIT_RATE,
            DEFAULT_LAME_MP3_QUALITY
        )
        // Create and run thread used to encode data
        // The thread will
        mEncodeThread = DataEncodeThread(mRecordFile, mBufferSize)
        mEncodeThread?.let {
            it.start()
            mAudioRecord?.setRecordPositionUpdateListener(it, it.getHandler())
            mAudioRecord?.setPositionNotificationPeriod(FRAME_COUNT)
        }
    }

    /**
     * 此计算方法来自samsung开发范例
     *
     * @param buffer   buffer
     * @param readSize readSize
     */
    private fun calculateRealVolume(buffer: ShortArray, readSize: Int) {
        var sum = 0.0
        for (i in 0 until readSize) {
            // 这里没有做运算的优化，为了更加清晰的展示代码
            sum += (buffer[i] * buffer[i]).toDouble()
        }
        if (readSize > 0) {
            val amplitude = sum / readSize
            mVolume = sqrt(amplitude).toInt()
        }
        Log.d("CXP_LOG", "音量: $mVolume")
    }


    /**
     * 获取真实的音量。 [算法来自三星]
     * @return 真实音量
     */
    fun getRealVolume(): Int {
        return mVolume
    }

    /**
     * 获取相对音量。 超过最大值时取最大值。
     * @return 音量
     */
    fun getVolume(): Int {
        return if (mVolume >= ToneGenerator.MAX_VOLUME) {
            ToneGenerator.MAX_VOLUME
        } else mVolume
    }

    /**
     * 根据资料假定的最大值。 实测时有时超过此值。
     * @return 最大音量值。
     */
    fun getMaxVolume() = MAX_VOLUME

    fun stop() {
        mIsRecording = false
    }

    fun isRecording() = mIsRecording

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

        //最大音量
        private const val MAX_VOLUME = 2000

        /**
         * 删除文件
         */
        fun deleteFile(filePath: String) {
            val file = File(filePath)
            if (file.exists()) {
                if (file.isFile) {
                    file.delete()
                } else {
                    val filePaths: Array<String> = file.list()
                    filePaths.forEach {
                        deleteFile("$filePath/$it")
                    }
                    file.delete()
                }
            }
        }
    }


}