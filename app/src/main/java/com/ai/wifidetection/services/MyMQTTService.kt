package com.ai.wifidetection.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.ai.wifidetection.MyBuffer
import com.ai.wifidetection.MyBuffer.is_service_working
import com.ai.wifidetection.MyConstant
import com.ai.wifidetection.MyConstant.MQTT_SERVER_PWD
import com.ai.wifidetection.MyConstant.MQTT_SERVER_USERNAME
import com.ai.wifidetection.TOPIC_ALL
import com.ai.wifidetection.beans.mqttbeans.DetResultBean
import com.ai.wifidetection.beans.mqttbeans.DeviceListBean
import com.ai.wifidetection.beans.mqttbeans.NotifySubscribeBean
import com.ai.wifidetection.beans.mqttbeans.RunStatusBean
import com.ai.wifidetection.utils.AppTools
import com.ai.wifidetection.utils.LogUtil
import com.google.gson.Gson
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Exception

class MyMQTTService : Service() {
    val TAG = MyMQTTService::class.java.simpleName

    private var connectOptions: MqttConnectOptions? = null
    private var macString: String = ""
    private val TAG2 = "MyMQTTService"
    private var myTopics = mutableListOf<String>()


    internal inner class MyCustomBinder : Binder() {
        val instance: MyMQTTService
            get() = this@MyMQTTService
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        LogUtil.d("onCreate")
        clientId = AppTools.getMac(this)//客户端标识
        init()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    private fun init() {
        // 服务器地址（协议+地址+端口号）
        val serverURL = "tcp://${MyConstant.MQTT_SERVER_IP}:${MyConstant.MQTT_SERVER_PORT}"
        androidClient = MqttAndroidClient(applicationContext, serverURL, clientId)
        androidClient!!.setCallback(mMQTTCallback)             // 设置MQTT监听并且接受消息
        connectOptions = MqttConnectOptions()
        // 清除缓存 是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
        connectOptions!!.isCleanSession = true
        connectOptions!!.connectionTimeout = 10                 // 设置超时时间，单位：秒
        connectOptions!!.keepAliveInterval = 60                 // 心跳包发送间隔，单位：秒
        connectOptions!!.isAutomaticReconnect = true            //断开后，是否自动连接
        connectOptions!!.userName = MQTT_SERVER_USERNAME        // 用户名
        connectOptions!!.password = MQTT_SERVER_PWD.toCharArray()      // 密码   将字符串转换为字符串数组
        // 设置MQTT版本号，以防某些平台的MQTT服务器校验不通过
        connectOptions!!.mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1_1

        // last will message
//        lastWill()
        doClientConnection()
    }

    // MQTT是否连接成功
    private val iMQTTActionListener = object : IMqttActionListener {
        override fun onSuccess(arg0: IMqttToken) {
            Log.i(TAG, "连接成功 ")
        }
        override fun onFailure(arg0: IMqttToken?, arg1: Throwable) {
            Log.i(TAG, " 连接失败 ")
            arg1.printStackTrace()
            isConnectOk = false
            if (is_service_working) {
                Handler().postDelayed({
                    doClientConnection()
                }, 3000)
            }
        }
    }

    // MQTT 监听并且接受消息
    private val mMQTTCallback: MqttCallback = object : MqttCallbackExtended {
        override fun connectComplete(reconnect: Boolean, serverURI: String?) {
            LogUtil.d("connectComplete: 连接完成  serverURI: $serverURI")
            try {
                isConnectOk = true
                val status = RunStatusBean()
                status.isConnectCompile = true
                EventBus.getDefault().post(status)
                subscribeToTopic()
            }catch (e: MqttException){
                e.printStackTrace()
            }
        }
        override fun messageArrived(topic: String, message: MqttMessage) {
            // 订阅的消息送达，推送notify
            val payload = String(message.payload)
            val str2 = topic + ";qos:" + message.qos + ";retained:" + message.isRetained
//            Log.i(TAG, "messageArrived:$payload")
            when(topic){
                myTopics[0]->{
                    val messageArray = payload.split(",")
                    if (messageArray.size >= 2){
                        val detResult = DetResultBean()
                        detResult.result = messageArray[0].toInt()
                        detResult.value = messageArray[1].toInt()
                        if (messageArray.size>=3) {
                            detResult.score = messageArray[2].toInt()
                        }
                        EventBus.getDefault().post(detResult)
                    }
                }
                myTopics[1]->{
                    try {
                        Log.i(TAG, str2)
                        val resultBean = Gson().fromJson(payload, DeviceListBean::class.java)
                        if (null != resultBean && resultBean.msg == "success"){
                            EventBus.getDefault().post(resultBean)
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
                else->{
                    LogUtil.e("mqtt return: unknown command!!!")
                }
            }

        }
        override fun deliveryComplete(arg0: IMqttDeliveryToken) {
            LogUtil.d("deliveryComplete: connection was deliveryComplete")
        }

        override fun connectionLost(arg0: Throwable) {
            // 失去连接，重连
            LogUtil.e("connectionLost: connection was lost")
            val status = RunStatusBean()
            status.isConnectCompile = false
            EventBus.getDefault().post(status)
            if (is_service_working) {
                Handler().postDelayed({
                    doClientConnection()
                }, 3000)
            }
        }
    }

    /** 判断网络是否连接  */
    private val isConnectIsNormal: Boolean
        get() {
            val connectivityManager = this.applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivityManager.activeNetworkInfo
            return if (info != null && info.isAvailable) {
                val name = info.typeName
                LogUtil.d(TAG, "MQTT当前网络名称：$name")
                true
            } else {
                LogUtil.e("MQTT 没有可用网络")
                isConnectOk = false
                false
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        try {
            if (null != androidClient && androidClient!!.isConnected) {
                androidClient!!.setCallback(null)
                androidClient!!.disconnect()
                EventBus.getDefault().unregister(this)
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
        LogUtil.d("onDestroy")
    }

    /** 连接MQTT服务器  */
    fun doClientConnection() {
        LogUtil.d("doClientConnection is_work=$is_service_working")
        if (is_service_working) {
            if (!androidClient!!.isConnected && isConnectIsNormal) {
                try {
                    // 参数options：用来携带连接服务器的一系列参数，例如用户名、密码等
                    // 参数userContext：可选对象，用于向回调传递上下文。一般传null即可。
                    // 参数callback：用来监听MQTT是否连接成功的回调
                    androidClient!!.connect(connectOptions, null, iMQTTActionListener)
                } catch (e: MqttException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Binder 进程间通信机制
     * 提供被对外调用的 MQTTService实例
     */
    override fun onBind(intent: Intent): IBinder? {
        LogUtil.d("onBind")
        return null
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun notifySubscribeByActivities(notifySubscribeBean: NotifySubscribeBean){
        if (!isConnectOk){
            return
        }
        if (notifySubscribeBean.isSubscribe){
            subscribeToTopic(notifySubscribeBean.macString)
        }else{
            unSubscribeToTopic()
        }
    }

    //订阅消息
    fun subscribeToTopic() {
//        macString = SharedPrefsUtil.getValue(this, "mac", "").toString()
        macString = MyBuffer.current_mac
        LogUtil.d("1. subscribeToTopic====$macString")
        subscribeToTopic(macString)
    }
    //订阅消息
    private fun subscribeToTopic(macString: String?) {
        LogUtil.d("2. subscribeToTopic====$macString")
        if (macString.isNullOrEmpty()){
            return
        }
        try {
            myTopics.clear()
            myTopics.add(String.format(TOPIC_ALL.topic_device_result, macString))
            myTopics.add(String.format(TOPIC_ALL.topic_device_list, macString))
            val qoss = mutableListOf<Int>()
            for (i in 0 until myTopics.size){
                qoss.add(2)
            }
            LogUtil.d("subscribeToTopic:  $myTopics")
            if (null == androidClient){
                LogUtil.e("subscribeToTopic failed!!!, androidClient is not init ok")
                return
            }
            androidClient?.subscribe(myTopics.toTypedArray(), qoss.toIntArray(), null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    LogUtil.d("onSuccess: Success to Subscribed!")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable) {
                    LogUtil.e("onFailure: Failed to subscribe")
                }
            })
        } catch (ex: MqttException) {
            LogUtil.e("subscribeToTopic: Exception whilst subscribing")
            ex.printStackTrace()
        }
    }

    private fun unSubscribeToTopic(){
        androidClient!!.unsubscribe(myTopics.toTypedArray(), null, object : IMqttActionListener{
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                LogUtil.d("onSuccess: Success to unSubscribed!")
                myTopics.clear()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable) {
                LogUtil.e("onFailure: Failed to unsubscribe")
            }

        })
    }


    //对外 相当于Java的static
    companion object {

        private var androidClient: MqttAndroidClient? = null
        private var clientId: String? = null
        private var isConnectOk = false
        private var qos = 0

        private var publishTopic = "/mtk_devlist/"

        fun publish(msg: String, mac:String) {
            val topic = publishTopic + mac
            val retained = false
            try {
                if (null != androidClient && isConnectOk) {
                    LogUtil.e("$topic   message: $msg")
                    androidClient!!.publish(topic, msg.toByteArray(), qos, retained)
                }else{
                    LogUtil.i("client not init ok!")
                }
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }

        private fun publishByTopic(msg: String, topic: String) {
            val retained = false
            try {
                if (null != androidClient && androidClient!!.isConnected) {
                    androidClient!!.publish(topic, msg.toByteArray(), qos, retained)
                }
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }

    }
}
