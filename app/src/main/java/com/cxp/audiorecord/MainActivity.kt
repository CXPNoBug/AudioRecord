package com.cxp.audiorecord

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cxp.audiorecord.databinding.ActivityMainBinding
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val TAG = "CXP_LOG"

    private lateinit var binding: ActivityMainBinding

    //音频输入-麦克风
    private val AUDIO_INPUT = MediaRecorder.AudioSource.MIC

    /**
     * 采用频率
     * 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
     * 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
     */
    private val AUDIO_SAMPLE_RATE = 16000

    //声道 单声道
    private val AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO

    //编码
    private val AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT

    // 缓冲区字节大小
    private var bufferSizeInBytes = 0

    //录音对象
    private var audioRecord: AudioRecord? = null

    //录音状态
    private var status = STATUS_NO_READY

    //文件名
    private var fileName = ""

    //录音文件
    private val filesName: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.recordTv.text = ""

        binding.startRecord.setOnClickListener {
            PermissionX.init(this)
                .permissions(Manifest.permission.RECORD_AUDIO)
                .request { allGranted, _, _ ->
                    if (allGranted) {
                        //初始化录制
                        initRecord()
                        //开始录制
                        startRecord()
                    }
                }
        }

        binding.pauseRecord.setOnClickListener {
            if (status == STATUS_PAUSE) {
                binding.pauseRecord.text = "暂停录音"
                startRecord()
            } else {
                binding.pauseRecord.text = "继续录音"
                pauseRecord()
            }
        }
        binding.stopRecord.setOnClickListener {
            //停止录制
            stopRecord()
        }
    }

    /**
     * 初始化录制
     */
    private fun initRecord() {
        fileName = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(Date())
        bufferSizeInBytes = AudioRecord.getMinBufferSize(
            AUDIO_SAMPLE_RATE,
            AUDIO_CHANNEL, AUDIO_ENCODING
        )
        audioRecord = AudioRecord(
            AUDIO_INPUT,
            AUDIO_SAMPLE_RATE,
            AUDIO_CHANNEL,
            AUDIO_ENCODING,
            bufferSizeInBytes
        )
        status = STATUS_READY
    }

    /**
     * 开始录制
     */
    private fun startRecord(listener: RecordStreamListener? = null) {
        if (status == STATUS_NO_READY || fileName.isBlank()) {
            throw IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~")
        }
        if (status == STATUS_START) {
            throw IllegalStateException("正在录音")
        }
        Log.d(TAG, "=====================开始录制=====================")
        audioRecord?.startRecording()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                writeDataFile(listener)
            }
        }
    }

    /**
     * 暂停录制
     */
    private fun pauseRecord() {
        Log.d(TAG, "================暂停录制====================")
        if (status != STATUS_START) {
            throw IllegalStateException("非录音状态")
        } else {
            audioRecord?.stop()
            status = STATUS_PAUSE
        }
    }

    /**
     * 停止录制
     */
    private fun stopRecord() {
        Log.d(TAG, "================停止录制====================")
        if (status == STATUS_NO_READY || status == STATUS_READY) {
            throw IllegalStateException("录音尚未开始")
        } else {
            audioRecord?.stop()
            status = STATUS_STOP
            release()
        }
    }

    /**
     * 取消录音
     */
    private fun cancel() {
        filesName.clear()
        audioRecord?.release()
        audioRecord = null
    }

    /**
     * 释放资源
     */
    private fun release() {
        Log.d(TAG, "======================释放资源========================")
        try {
            if (filesName.size > 0) {
                val filePaths = mutableListOf<String>()
                filesName.forEach {
                    filePaths.add(FileUtil.getPcmFilePath(it))
                }
                //清除
                filesName.clear()
                if (filePaths.isNotEmpty()) {
//                    if (filePaths.size==1) {
//                        makePcmFileToWavFile()
//                    }else{
                    //将多个pcm文件转化为wav文件
                    mergePcmFilesToWavFile(filePaths)
//                    }
                }
            }
            audioRecord?.release()
            audioRecord = null
            status = STATUS_NO_READY
        } catch (e: Exception) {
            Log.d(TAG, "release:${e.message}")
        }
    }

    /**
     * 将pcm合并成wav
     */
    private fun mergePcmFilesToWavFile(filePaths: List<String>) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (PcmToWav.mergePcmFilesToWavFile(filePaths, FileUtil.getWavFilePath(fileName))) {
                    //操作成功
                    Log.d(TAG, "================WAV 转换成功====================")
                } else {
                    //操作失败
                    Log.d(TAG, "================WAV 转换失败====================")
                }
                fileName = ""
            }
        }
    }

    /**
     * 将单个pcm文件转化为wav文件
     */
    private fun makePcmFileToWavFile() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (PcmToWav.makePcmFileToWavFile(
                        FileUtil.getPcmFilePath(fileName),
                        FileUtil.getWavFilePath(fileName),
                        true
                    )
                ) {
                    //操作成功
                    Log.d(TAG, "================WAV 转换成功====================")
                } else {
                    //操作失败
                    Log.d(TAG, "================WAV 转换失败====================")
                }
                fileName = ""
            }
        }
    }

    /**
     * 将音频信息写入文件
     */
    private fun writeDataFile(listener: RecordStreamListener? = null) {
        //创建一个byte数组用来存一些字节数据，大小为缓冲区大小
        val audioData = ByteArray(bufferSizeInBytes)
        val fos: FileOutputStream
        var readSize = 0
        try {
            var currentFileName = fileName
            if (status == STATUS_PAUSE) {
                //假如是暂停录音 将文件名后面加个数字,防止重名文件内容被覆盖
                currentFileName += filesName.size
            }
            filesName.add(currentFileName)
            val file = File(FileUtil.getPcmFilePath(currentFileName))
            if (file.exists()) {
                file.delete()
            }
            //建立一个可存取字节的文件
            fos = FileOutputStream(file)
            fos.use {
                //将录音状态设置成正在录音状态
                status = STATUS_START
                audioRecord?.let {
                    while (status == STATUS_START) {
                        readSize = it.read(audioData, 0, bufferSizeInBytes)
                        if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                            fos.write(audioData)
                            listener?.recordOfByte(audioData, 0, audioData.size)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(
                TAG, "=====================写入文件失败=====================\n${e.message}"
            )
        }
    }

    companion object {
        //未开始
        const val STATUS_NO_READY = 0

        //预备
        const val STATUS_READY = 1

        //录音
        const val STATUS_START = 2

        //暂停
        const val STATUS_PAUSE = 3

        //停止
        const val STATUS_STOP = 4
    }


}