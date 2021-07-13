package com.czt.mp3recorder

import android.media.AudioRecord

/**
 * <pre>
 *     author : ChengPeng
 *     e-mail : cxpnobug@gmail.com
 *     time   : 2021/07/13
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class DataEncodeThread :AudioRecord.OnRecordPositionUpdateListener {

    override fun onMarkerReached(recorder: AudioRecord?) {
    }

    override fun onPeriodicNotification(recorder: AudioRecord?) {
    }
}