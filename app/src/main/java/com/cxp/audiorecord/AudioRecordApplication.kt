package com.cxp.audiorecord

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * <pre>
 *     author : ChengPeng
 *     e-mail : cxpnobug@gmail.com
 *     time   : 2021/07/12
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class AudioRecordApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}