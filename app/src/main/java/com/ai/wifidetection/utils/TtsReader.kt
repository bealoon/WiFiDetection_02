@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.ai.wifidetection.utils

import android.content.Context
import android.content.res.AssetManager
import android.media.MediaPlayer
import android.util.Log


//TTS 语音播放
object TtsReader {
    private var mMediaPlayer: MediaPlayer? = null

    private var isTtsFinish = true
    private var lastType = 0

    fun getMediaPlayer(): MediaPlayer? {
        if (null == mMediaPlayer) {
            mMediaPlayer = MediaPlayer()
        }
        return mMediaPlayer
    }

    private fun playAudio(mContext: Context, fileName: String) {
        isTtsFinish = false
        try {
            stopAudio();//如果正在播放就停止
            val assetManager: AssetManager = mContext.assets
            val afd = assetManager.openFd(fileName)
            val mediaPlayer = getMediaPlayer()
            mediaPlayer!!.setDataSource(
                afd.fileDescriptor,
                afd.startOffset,
                afd.length
            )
            mediaPlayer.isLooping = false //循环播放
            mediaPlayer.prepare()
            mediaPlayer.setOnPreparedListener { mediaPlayer -> mediaPlayer.start() }

            return
        } catch (e : Exception) {
            e.printStackTrace()
            Log.e("播放音频失败",e.message);
        }finally {
            isTtsFinish = true
        }
    }

    /**
     * 停止播放音频
     */
    private fun stopAudio() {
        try {
            if (null != mMediaPlayer) {
                if (mMediaPlayer!!.isPlaying) {
                    mMediaPlayer!!.pause()
                    mMediaPlayer!!.reset()
                    mMediaPlayer!!.stop()
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e("stopAudio", "")
        }
    }

    fun tts(paramContext: Context, warningType: Int) {
//        if (lastType == warningType){
//            return
//        }
//        if (AppTools.isTablet(paramContext)){//平板
//            when(warningType) {
//                Constant.WARNING_TYPE.security -> playSound(paramContext, "radio_chuangru.mp3")
//                Constant.WARNING_TYPE.evacuation -> playSound(paramContext, "radio_huojing.mp3")
//                Constant.WARNING_TYPE.fall -> playSound(paramContext, "radio_shuaidao.mp3")
//                Constant.WARNING_TYPE.retention -> playSound(paramContext, "radio_zhiliu.mp3")
//            }
//        }else{//手机
//            when(warningType) {
//                Constant.WARNING_TYPE.security -> playSound(paramContext, "radio_chuangru_phone.mp3")
//                Constant.WARNING_TYPE.evacuation -> playSound(paramContext, "radio_huojing_phone.mp3")
//                Constant.WARNING_TYPE.fall -> playSound(paramContext, "radio_shuaidao.mp3")
//                Constant.WARNING_TYPE.retention -> playSound(paramContext, "radio_zhiliu.mp3")
//            }
//        }

        lastType = warningType
    }
}
