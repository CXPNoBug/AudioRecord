package com.cxp.audiorecord

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cxp.audiorecord.databinding.ActivityMp3Binding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMp3Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}