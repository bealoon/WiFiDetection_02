package com.ai.wifidetection.ui.activities

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.ai.wifidetection.AppPermissionCode
import com.ai.wifidetection.MyBuffer
import com.ai.wifidetection.R
import com.ai.wifidetection.beans.mqttbeans.COMMAND_UPLOAD
import com.ai.wifidetection.beans.mqttbeans.DeviceListBean
import com.ai.wifidetection.beans.mqttbeans.NotifySubscribeBean
import com.ai.wifidetection.services.MyMQTTService
import com.ai.wifidetection.ui.base.BaseActivity
import com.ai.wifidetection.utils.*
import com.ai.wifidetection.utils.statusbar.StatusBarUtil
import com.yzq.zxinglibrary.android.CaptureActivity
import com.yzq.zxinglibrary.bean.ZxingConfig
import com.yzq.zxinglibrary.common.Constant
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_start.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class StartActivity : BaseActivity() {

    private var macSta: String? = null
//    private var macAP: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        //用来设置整体下移，状态栏沉浸
        StatusBarUtil.setRootViewFitsSystemWindows(this, true)

        EventBus.getDefault().register(this)

        btn_link_scan_ap.setOnClickListener {
            PermissionUtils.checkPermission(this, CAMERA, MyPermissionCheckCallBack())
        }

        btn_link_input_ap.setOnClickListener {
            goInputDialog()
        }

        btn_link_current_ap.setOnClickListener {
            PermissionUtils.checkAndRequestPermission(this, ACCESS_FINE_LOCATION, AppPermissionCode.CODE_PERMISSION_FINE_LOCATION) { // 权限已被授予
                goGetCurrentApMac()
            }
        }
    }

    inner class MyPermissionCheckCallBack : PermissionUtils.PermissionCheckCallBack{
        override fun onHasPermission() {
            goScan()
        }

        override fun onUserHasAlreadyTurnedDown(vararg permission: String?) {
            PermissionUtils.requestPermission(this@StartActivity, CAMERA, AppPermissionCode.CODE_PERMISSION_CAMERA)
        }

        override fun onUserHasAlreadyTurnedDownAndDontAsk(vararg permission: String?) {
            Toasty.warning(this@StartActivity, "未获取到CAMERA权限,请前往设置").show()
        }

    }

    override fun onStart() {
        super.onStart()
        // 获取设备宽度、
        AppTools.setScreenWidth(this)
    }

    private fun goGetCurrentApMac() {
        if (!NetWorkUtils.isWifiOpen(this)) {
            Toasty.error(this, "未开启WiFi设置!").show()
            return
        }
        if (!NetWorkUtils.isWifiByType(this)) {
            Toasty.error(this, "当前未连接到WiFi!").show()
            return
        }
        val macString = AppTools.getMacFromWifiAP(this)
        if (macString.isNullOrEmpty()) {
            Toasty.error(this, "获取路由器地址失败，请重试").show()
            return
        }
        val mac = macString.replace(":", "").replace("-", "")
        if (!isMacString(mac)) {
            Toasty.error(this, "获取路由器地址失败，请重试").show()
            return
        }
        checkMacPrecess(mac)
    }

    private fun goScan() {

        val intent = Intent(this, CaptureActivity::class.java)
        val config = ZxingConfig()
        config.isPlayBeep = false //是否播放扫描声音 默认为true
        config.isShake = true //是否震动  默认为true
        config.isDecodeBarCode = true //是否扫描条形码 默认为true
        config.reactColor = R.color.colorPrimaryDark //设置扫描框四个角的颜色 默认为白色
        config.frameLineColor = R.color.colorPrimary //设置扫描框边框颜色 默认无色
        config.scanLineColor = R.color.colorAccent //设置扫描线的颜色 默认白色

        config.isFullScreenScan = false //是否全屏扫描  默认为true  设为false则只会在扫描框中扫描

        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config)
        startActivityForResult(intent, AppPermissionCode.CODE_REQ_QR)
        showLoadingDialog()
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        dismissLoadingDialog()
        // 扫描二维码/条码回传
        if (requestCode == AppPermissionCode.CODE_REQ_QR && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val macString = data.getStringExtra(Constant.CODED_CONTENT)
                if (null != macString && macString.length >= 12) {
                    val mac = macString.replace(":", "").replace("-", "")
                    Toasty.success(applicationContext, "扫描路由器成功!").show()
                    checkMacPrecess(mac)
                } else {
                    Toasty.error(applicationContext, "扫描结果不匹配!").show()
                }
            } else {
                Toasty.error(applicationContext, "未扫描到设备!").show()
            }
        }
    }

    private fun goInputDialog() {
        val factory = LayoutInflater.from(this)
        val textEntryView = factory.inflate(R.layout.layout_dialog_input_mac, null)
        val editText1 = textEntryView.findViewById(R.id.editText1) as EditText
        val macString = SharedPrefsUtil.getValue(this, "mac", "") as String
        editText1.setText(macString)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("请输入路由器mac地址")
        builder.setView(textEntryView)
        builder.setCancelable(false)
        builder.setPositiveButton("确定") { dialog, which ->
            run {
                val macString = editText1.text.toString()
                if (macString.length < 12) {
                    Toasty.info(this@StartActivity, "输入长度不足12位").show()
                    return@setPositiveButton
                }
                checkMacPrecess(macString)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("取消") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }


    private fun isMacString(macString: String): Boolean {
        LogUtil.i(macString)
        if (macString == "020000000000") {
            return false
        }
        //^([0-9A-F]{2}[:]){5}([0-9A-F]{2})$
        val regex = Regex("([0-9a-fA-F]){12}")
        return macString.matches(regex = regex)
    }

    private fun checkMacPrecess(macString: String) {
        if (!NetWorkUtils.isConnectedByState(this)) {
            runOnUiThread {
                Toasty.info(this@StartActivity, "请检查网络").show()
            }
            return
        }
        if (isMacString(macString)) {
            getDeviceList(macString)
        } else {
            Toasty.error(this@StartActivity, "Mac格式有误！请重试").show()
        }
    }

    private fun goNextActivity(macApString: String){
        val intent = Intent(this, Main2Activity::class.java)
        intent.putExtra("mac_ap", macApString)
        intent.putExtra("mac_sta", macSta)
        MyBuffer.current_mac = macApString
        SharedPrefsUtil.putValue(this, "mac", macApString)
        startActivity(intent)
        this.finish()
    }

    private fun getDeviceList(macString: String){
        // 通知启动MQTT主题监听
        val bean = NotifySubscribeBean()
        bean.isSubscribe = true
        bean.macString = macString
        EventBus.getDefault().post(bean)

        //查询正在采集得ping设备mac
        val topic1 = "{\"command\":\"query_mac\"}"
        MyMQTTService.publish(topic1, macString)
        showLoadingDialog()
        Handler().postDelayed({
            dismissLoadingDialog()
            goNextActivity(macString)
        }, 1000)
    }

    /**
     * 接收设备列表的mqtt消息
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun mqttArrived(messageEvent: DeviceListBean?){
        println(">>>mqttArrived "+ messageEvent!!.command)
        when(messageEvent.command!!){
            COMMAND_UPLOAD.query_mac->{
                macSta = messageEvent.mac
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}

