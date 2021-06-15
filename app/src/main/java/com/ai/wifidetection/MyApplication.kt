package com.ai.wifidetection

import android.R
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.ai.wifidetection.MyBuffer.is_service_working
import com.ai.wifidetection.services.MyMQTTService
import com.ai.wifidetection.ui.activities.StartActivity
import com.ohmerhe.kolley.request.Http


class MyApplication : Application(){
    var TAG = "MyApplication"

    var mMQTTIntent: Intent? = null
    var mMQTTService: MyMQTTService? = null
    var mSerConn: MyServiceConnection? = null

    override fun onCreate() {
        super.onCreate()
        Http.init(this)
        startMqttService()

        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true) //default: true
            .showErrorDetails(true) //default: true
            .showRestartButton(true) //default: true
            .logErrorOnRestart(false) //default: true
            .trackActivities(true) //default: false
            .minTimeBetweenCrashesMs(2000) //default: 3000
            .errorDrawable(com.ai.wifidetection.R.mipmap.data_error) //default: bug image
            .restartActivity(StartActivity::class.java) //default: null (your app's launch activity)
            .apply()
    }

    fun startMqttService(){
        //启动MQTT服务
        Log.e(TAG, "startMqttService")
        is_service_working = true
        mSerConn = MyServiceConnection()
        mMQTTIntent = Intent(this, MyMQTTService::class.java)
        bindService(mMQTTIntent, mSerConn!!, Context.BIND_AUTO_CREATE)
    }

    fun stopMqttService(){
        Log.e(TAG, "stopMqttService")
        is_service_working = false
        Handler().postDelayed({
            unbindService(mSerConn!!)
            mMQTTService = null
            mMQTTIntent = null
        }, 500)
    }

    // 获取服务实例，对外公开；
    // 便于全局使用
    inner class MyServiceConnection: ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            println("MyServiceConnection===onServiceDisconnected")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            println("MyServiceConnection===onServiceConnected  $name")
//            mMQTTService = (service as MyMQTTService.MyCustomBinder).instance
        }

    }
}