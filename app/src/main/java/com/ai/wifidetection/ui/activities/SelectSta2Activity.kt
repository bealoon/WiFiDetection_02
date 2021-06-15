package com.ai.wifidetection.ui.activities

import android.app.ActionBar
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.ai.wifidetection.CODE_RESULT_ACTIVITY_SELECT_STATION
import com.ai.wifidetection.MODE_SELECT2_STA
import com.ai.wifidetection.R
import com.ai.wifidetection.beans.mqttbeans.COMMAND_UPLOAD
import com.ai.wifidetection.beans.mqttbeans.DeviceListBean
import com.ai.wifidetection.services.MyMQTTService
import com.ai.wifidetection.ui.adapters.DeviceGVAdapter
import com.ai.wifidetection.ui.base.BaseActivity
import com.ai.wifidetection.utils.LogUtil
import com.ai.wifidetection.utils.SharedPrefsUtil
import com.ai.wifidetection.utils.statusbar.StatusBarUtil
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_select_sta2.*
import kotlinx.android.synthetic.main.activity_select_station.*
import kotlinx.android.synthetic.main.activity_select_station.gv_device
import kotlinx.android.synthetic.main.activity_select_station.refreshLayout
import kotlinx.android.synthetic.main.layout_title_bar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class SelectSta2Activity : BaseActivity() {
    val CONST_MODE_SELECT = "select_mode"

    var macAP: String = ""
    var macSta: String = ""
    var mSelectStaMode: Int = MODE_SELECT2_STA.auto
    var mRVAdapter: DeviceGVAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_sta2)
        //用来设置整体下移，状态栏沉浸
        StatusBarUtil.setRootViewFitsSystemWindows(this, true)

        macAP = intent.getStringExtra("mac_ap")!!


        initView()
        initData()

        initListener()
    }

    private fun initData() {
        mSelectStaMode = SharedPrefsUtil.getValue(this, CONST_MODE_SELECT, 0)
        selectStaModeAuto((mSelectStaMode == MODE_SELECT2_STA.auto))
    }

    private fun initView() {
        initTitleBar()
        initRecycleList()
    }

    private fun initListener() {
        EventBus.getDefault().register(this)
        btn_select2_confirm.setOnClickListener {
            onBackPressed()
        }

        rb_select2_auto.setOnCheckedChangeListener { buttonView, isChecked ->
            selectStaModeAuto(true)
        }

        cb_select2_manual.setOnCheckedChangeListener { buttonView, isChecked ->
            selectStaModeAuto(false)
        }
    }

    private fun selectStaModeAuto(isAuto: Boolean){
        if (isAuto){
            refreshLayout.isEnabled = false
            refreshLayout.isClickable = false
            gv_device.isEnabled = false
            mSelectStaMode = MODE_SELECT2_STA.auto
        }else{
            refreshLayout.isEnabled = true
            refreshLayout.isClickable = true
            gv_device.isEnabled = true
            mSelectStaMode = MODE_SELECT2_STA.manual
        }
        rb_select2_auto.isSelected = isAuto
        cb_select2_manual.isSelected = !isAuto
        SharedPrefsUtil.putValue(this, CONST_MODE_SELECT, mSelectStaMode)
    }

    override fun onStart() {
        super.onStart()
        // 获取设备列表
        MyMQTTService.publish("{\"command\":\"query_list\"}", macAP)
        //查询ping设备
        val topic_query_mac = "{\"command\":\"query_mac\"}"
        MyMQTTService.publish(topic_query_mac, macAP)
    }

    /**
     * 初始化list
     */
    private fun initRecycleList() {
        mRVAdapter = DeviceGVAdapter(this)
        gv_device.adapter = mRVAdapter
        gv_device.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val deviceList = mRVAdapter!!.mDeviceList
                for (i in deviceList!!.indices){
                    deviceList[i].is_selected = (i == position)
                }
                mRVAdapter!!.notifyDataSetChanged()
                // 获取设备列表
//                macSta = deviceList[position].mac!!.replace(":","").replace("-","")
                macSta = deviceList[position].mac!!
                val topic0 = "{\"command\":\"req_set_csi_mac\", \"mac\":\"%s\", \"ip\":\"%s\"}".format(macSta, deviceList[position].ip)
                MyMQTTService.publish(topic0, macAP)
                //查询ping设备
                val topic1 = "{\"command\":\"query_mac\"}"
                MyMQTTService.publish(topic1, macAP)
            }

        refreshLayout.setOnRefreshListener {
            MyMQTTService.publish("{\"command\":\"query_list\"}", macAP)
            //查询ping设备
            val topic1 = "{\"command\":\"query_mac\"}".format(macSta)
            MyMQTTService.publish(topic1, macAP)
            it.finishRefresh(1000 /*,false*/) //传入false表示刷新失败
        }
    }

    /**
     * 接收设备列表的mqtt消息
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun mqttArrived(messageEvent: DeviceListBean?){
        println(">>>mqttArrived "+ messageEvent!!.command)
        when(messageEvent.command!!){
            COMMAND_UPLOAD.query_list->{
                mRVAdapter!!.mDeviceList = messageEvent.sta!!.toMutableList()
                mRVAdapter!!.notifyDataSetChanged()
            }
            COMMAND_UPLOAD.query_mac->{
                var isQueryOk = false
                for (i in mRVAdapter!!.mDeviceList!!.indices){
                    if (mRVAdapter!!.mDeviceList?.get(i)?.mac == messageEvent.mac && mRVAdapter!!.mDeviceList?.get(i)?.ip == messageEvent.ip) {
                        mRVAdapter!!.mDeviceList?.get(i)?.is_selected = true
                        isQueryOk = true
                    }else{
                        mRVAdapter!!.mDeviceList?.get(i)?.is_selected = false
                    }
                }
                if (isQueryOk){
                    runOnUiThread {
                        mRVAdapter!!.notifyDataSetChanged()
                        Toasty.success(this, "设置设备成功！").show()
                        macSta = messageEvent.mac!!
                    }
                }else{
                    macSta = ""
                }
            }
            COMMAND_UPLOAD.req_set_csi_mac->{
                LogUtil.e(messageEvent.msg)
            }
        }

    }

    private fun initTitleBar(){
        bar_title.text = "设备列表"
        bar_left.setOnClickListener {
            onBackPressed()
        }
        bar_right.visibility = View.INVISIBLE
    }

    override fun onBackPressed() {
        val backIntent = Intent()
        backIntent.putExtra("mac_sta", macSta)
        this.setResult(CODE_RESULT_ACTIVITY_SELECT_STATION)
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
