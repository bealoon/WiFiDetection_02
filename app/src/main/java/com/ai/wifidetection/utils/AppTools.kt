package com.ai.wifidetection.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.text.TextUtils
import android.util.DisplayMetrics
import java.io.IOException
import java.io.InputStreamReader
import java.io.LineNumberReader
import java.net.NetworkInterface
import java.util.*


object AppTools {

    /**
     * 判断是否是 平板 还是 手机
     * @return true : 平板 ; false : 手机
     */
    fun isTablet(activity: Context): Boolean {
        return (activity.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    /**
     * Android 6.0-Android 7.0 获取mac地址
     */
    //去空格
    // 赋予默认值
    private val macAddress: String?
        get() {
            var macSerial: String? = null
            var str: String? = ""

            try {
                val pp = Runtime.getRuntime().exec("cat/sys/class/net/wlan0/address")
                val ir = InputStreamReader(pp.inputStream)
                val input = LineNumberReader(ir)

                while (null != str) {
                    str = input.readLine()
                    if (str != null) {
                        macSerial = str.trim { it <= ' ' }
                        break
                    }
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            return macSerial
        }

    /**
     * Android 6.0 之前（不包括6.0）获取mac地址
     * 必须的权限 <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
     * @param context * @return
     */
    private fun getMacDefault(context: Context?): String? {
        var mac = ""
        if (context == null) {
            return mac
        }
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var info: WifiInfo? = null
        try {
            info = wifi.connectionInfo
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (info == null) {
            return null
        }
        mac = info.macAddress
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH)
        }
        return mac
    }

    /**
     * Android 7.0之后获取Mac地址
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET"></uses-permission>
     * @return
     */
    private fun getMacFromHardware(): String {
        try {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true))
                    continue
                val macBytes = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (!TextUtils.isEmpty(res1)) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    /**
     * 获取mac地址（适配所有Android版本）
     * @return
     */
    fun getMac(context: Context): String? {
        var mac: String? = ""
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault(context)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = macAddress
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mac = getMacFromHardware()
        }
        return mac
    }

    fun getMacFromWifiAP(context: Context): String?{
        var mac: String? = ""
        val mWifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (mWifi.isWifiEnabled){
            mac = mWifi.connectionInfo.bssid
            println("---------"+mWifi.connectionInfo.toString())
        }
        return mac
    }

    /**
     * 获取已连接的Wifi路由器的Mac地址
     */
    fun getConnectedWifiMacAddress(context: Context): String? {
        var connectedWifiMacAddress: String? = null
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var wifiList: List<ScanResult>? = null
        wifiList = wifiManager.scanResults
        println("=====$wifiList")
        val info = wifiManager.connectionInfo
        if (wifiList != null && info != null) {
            for (i in wifiList.indices) {
                val result: ScanResult = wifiList[i]
                if (info.bssid == result.BSSID) {
                    connectedWifiMacAddress = result.BSSID
                }
            }
        }
        return connectedWifiMacAddress
    }

    /**
     * 计算按照屏幕比例缩放后的尺寸
     *
     * @param context
     * @param size
     * @return
     */
    var w_screen: Int = 0
    fun setScreenWidth(context: Activity): Int {
        val screen = Point()
        context.windowManager.defaultDisplay.getSize(screen);
        w_screen = screen.x
        return w_screen
    }
}
