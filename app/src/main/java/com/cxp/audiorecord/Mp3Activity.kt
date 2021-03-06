package com.cxp.audiorecord

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cxp.audiorecord.databinding.ActivityMp3Binding
import com.czt.mp3recorder.MP3Recorder
import com.permissionx.guolindev.PermissionX
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * <pre>
 *     author : ChengPeng
 *     e-mail : cxpnobug@gmail.com
 *     time   : 2021/07/13
 *     desc   : mp3 音频录制
 *     version: 1.0
 * </pre>
 */
class Mp3Activity : AppCompatActivity() {

    private lateinit var binding: ActivityMp3Binding

    private var mRecord: MP3Recorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMp3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startRecord.setOnClickListener {
            PermissionX.init(this)
                .permissions(Manifest.permission.RECORD_AUDIO)
                .request { allGranted, _, _ ->
                    if (allGranted) {
                        try {
                            val fileName =
                                SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(Date())
                            mRecord = MP3Recorder(File(FileUtil.getMp3FilePath(fileName)))
                            mRecord?.start()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
        }
        binding.stopRecord.setOnClickListener {
            mRecord?.stop()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mRecord?.stop()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, Mp3Activity::class.java)
            context.startActivity(intent)
        }
    }
}