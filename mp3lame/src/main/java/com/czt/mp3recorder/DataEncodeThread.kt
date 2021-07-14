package com.czt.mp3recorder

import android.media.AudioRecord
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import com.czt.mp3recorder.util.LameUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * <pre>
 *     author : ChengPeng
 *     e-mail : cxpnobug@gmail.com
 *     time   : 2021/07/13
 *     desc   :
 *     version: 1.0
 *     address: https://www.cnblogs.com/ct2011/p/4080193.html
 * </pre>
 */
class DataEncodeThread(file: File?, bufferSize: Int) : HandlerThread("DataEncodeThread"),
    AudioRecord.OnRecordPositionUpdateListener {

    companion object {
        private const val PROCESS_STOP = 1
        private const val PROCESS_ERROR = 2
    }

    private var mHandler: StopHandler? = null
    private var mFileOutputStream: FileOutputStream? = null
    private var mPath = ""
    private var mMp3Buffer: ByteArray? = null
    private val mTask: MutableList<Task> = Collections.synchronizedList(mutableListOf())

    init {
        mFileOutputStream = FileOutputStream(file)
        mPath = file?.absolutePath ?: ""
        mMp3Buffer = ByteArray((7200 + (bufferSize * 2 * 1.25)).toInt())
    }

    override fun start() {
        super.start()
        mHandler = StopHandler(looper,this)
    }

    override fun onMarkerReached(recorder: AudioRecord?) {
    }

    override fun onPeriodicNotification(recorder: AudioRecord?) {
        //设置通知周期。 以帧为单位
        processData()
    }

    private fun check() {
        if (mHandler == null) {
            throw IllegalStateException()
        }
    }

    fun sendStopMessage() {
        check()
        mHandler?.sendEmptyMessage(PROCESS_STOP)
    }

    fun sendErrorMessage() {
        check()
        mHandler?.sendEmptyMessage(PROCESS_ERROR)
    }

    fun getHandler(): Handler? {
        check()
        return mHandler
    }

    /**
     * 从缓冲区中读取并处理数据，使用lame编码MP3
     *
     * @return 从缓冲区中读取的数据的长度
     * 缓冲区中没有数据时返回0
     */
    private fun processData(): Int {
        if (mTask.size > 0) {
            val task = mTask.removeAt(0)
            val buffer = task.data
            val readSize = task.readSize
            val encodedSize = LameUtil.encode(buffer, buffer, readSize, mMp3Buffer!!)
            if (encodedSize > 0) {
                try {
                    mFileOutputStream?.write(mMp3Buffer, 0, encodedSize)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return readSize
        }
        return 0
    }

    /**
     * Flush all data left in lame buffer to file
     */
    private fun flushAndRelease() {
        //将MP3结尾信息写入buffer中
        val flushResult = LameUtil.flush(mMp3Buffer!!)
        if (flushResult > 0) {
            try {
                mFileOutputStream.use {
                    mFileOutputStream?.write(mMp3Buffer!!, 0, flushResult)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                LameUtil.close()
            }

        }
    }

    fun addTask(rawData: ShortArray, readSize: Int) {
        mTask.add(Task(rawData, readSize))
    }

    class StopHandler(looper:Looper ,private val encodeThread: DataEncodeThread) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            if (msg.what == PROCESS_STOP) {
                Log.d("CXP_LOG", "currentThread:${Thread.currentThread().name}")
                //处理缓冲区中的数据
                while (encodeThread.processData() > 0)
                // Cancel any event left in the queue
                removeCallbacksAndMessages(null)
                encodeThread.flushAndRelease()
                looper.quit()
            } else if (msg.what == PROCESS_ERROR) {
                //处理缓冲区中的数据
                while (encodeThread.processData() > 0)
                // Cancel any event left in the queue
                removeCallbacksAndMessages(null)
                encodeThread.flushAndRelease()
                looper.quit()
                MP3Recorder.deleteFile(encodeThread.mPath)
            }
        }
    }

    class Task(rawData: ShortArray, val readSize: Int) {
        val data: ShortArray = rawData.clone()
    }

}