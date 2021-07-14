package com.cxp.audiorecord

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cxp.audiorecord.databinding.ActivityMp3Binding
import com.cxp.audiorecord.databinding.ActivityWavBinding
import com.cxp.audiorecord.wavrecord.AudioRecorder
import com.czt.mp3recorder.MP3Recorder
import com.permissionx.guolindev.PermissionX

/**
 * <pre>
 *     author : ChengPeng
 *     e-mail : cxpnobug@gmail.com
 *     time   : 2021/07/14
 *     desc   : Wav 录音
 *     version: 1.0
 * </pre>
 */
class WavActivity:AppCompatActivity() {
    private lateinit var binding: ActivityWavBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startRecord.setOnClickListener {
            PermissionX.init(this)
                .permissions(Manifest.permission.RECORD_AUDIO)
                .request { allGranted, _, _ ->
                    if (allGranted) {
                        //初始化录制
                        AudioRecorder.initRecord()
                        //开始录制
                        AudioRecorder.startRecord()
                    }
                }
        }

        binding.pauseRecord.setOnClickListener {
            AudioRecorder.pauseRecord()
        }
        binding.stopRecord.setOnClickListener {
            //停止录制
            AudioRecorder.stopRecord()
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, WavActivity::class.java)
            context.startActivity(intent)
        }
    }
}