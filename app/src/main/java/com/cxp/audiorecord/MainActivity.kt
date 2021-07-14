package com.cxp.audiorecord

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cxp.audiorecord.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.audioWav.setOnClickListener {
            WavActivity.start(this)
        }
        binding.audioMp3.setOnClickListener {
            Mp3Activity.start(this)
        }
    }

}